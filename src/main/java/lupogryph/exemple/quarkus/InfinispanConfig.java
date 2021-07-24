package lupogryph.exemple.quarkus;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.jgroups.JChannel;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.stack.Protocol;

import javax.enterprise.context.ApplicationScoped;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class InfinispanConfig {

    @ConfigProperty(name = "infinispan.adresse")
    public Optional<String> adresse;

    @ConfigProperty(name = "infinispan.port")
    public Integer port;

    @ConfigProperty(name = "infinispan.nat")
    public Boolean nat;

    @ConfigProperty(name = "infinispan.ext.adresse")
    public String extAdresse;

    @ConfigProperty(name = "infinispan.ext.port")
    public Integer extPort;

    @ConfigProperty(name = "infinispan.members")
    public List<String> members;

    @ConfigProperty(name = "infinispan.machine")
    public String machine;

    @ConfigProperty(name = "infinispan.persistance.active")
    public Boolean persistanceActive;

    @ConfigProperty(name = "infinispan.persistance.chemin")
    public String persistanceChemin;

    /**
     * Creation du cache manager Infinispan
     *
     * @return {@link EmbeddedCacheManager}
     * @throws Exception
     */
    @ApplicationScoped
    EmbeddedCacheManager cacheManager() throws Exception {

        /**
         * On transforme la propriété issue de la configuration infinispan.adresse en InetAddress pour la conf TCP
         * Si elle n'existe pas on prend l'adresse localhost à la place
         *
         * We transform the property from the infinispan.adresse configuration in InetAddress for the use in TCP
         * If it doesn't exist, we take the localhost address instead
         */
        InetAddress bindAdresse = adresse.isPresent() ? InetAddress.getByName(adresse.get()) : InetAddress.getLocalHost();


        /**
         * On transforme la liste issue de la configuration infinispan.mambers en list d'inetSocketAddress pour la conf TCPPING
         *
         * We transform the list from the infinispan.members configuration property in list of InetSocketAddress for the use in TCPPING
         */
        List<InetSocketAddress> initialHosts = members.stream()
                .map(h -> {
                    String host = h.substring(0, h.indexOf("["));
                    int port = Integer.parseInt(h.substring(h.indexOf("[") + 1, h.indexOf("]")));
                    return new InetSocketAddress(host, port);
                }).collect(Collectors.toList())
                ;


        log.info("Debut configuration infinispan avec\n" +
                "adresse: " + adresse + "\n" +
                "\t -> " + bindAdresse.toString() + "\n" +
                "port: " + port + "\n" +
                ( nat ?
                        "NAT\n" +
                                "\t adresse externe: " + extAdresse + "\n" +
                                "\t port externe: " + extPort
                        :
                        "LOCAL"
                ) + "\n" +
                "members: " + members + "\n" +
                "\t -> " + initialHosts.toString() + "\n" +
                "machine: " + machine + "\n" +
                (persistanceActive ?
                        "persistance chemin: " + persistanceChemin
                        :
                        ""
                ) + "\n"
        );


        /**
         * On déclare une nouvelle configuration globale
         *
         * We declare a new infinispan global configuration
         */
        GlobalConfigurationBuilder globalConfigurationBuilder = new GlobalConfigurationBuilder();


        /**
         * On déclare notre stack de protocoles
         *
         * We declare our protocols stack
         */
        TCP_NIO2 tcp_nio2 = new TCP_NIO2()
                .setBindAddress(bindAdresse)
                .setBindPort(port)
                .setPortRange(0);
        // nat = true si on est sur un réseau NAT, un conteneur docker est un réseau NAT
        if (nat) {
            tcp_nio2
                    .setValue("external_addr", InetAddress.getByName(extAdresse))
                    .setValue("external_port", extPort);
        }

        // On configure notre stack pour JGroups avec nos paramètres
        Protocol[] protocols = {
                tcp_nio2,
                new TCPPING()
                        .setInitialHosts(initialHosts)
                        .portRange(0)
                        .setErgonomics(false),
                new MERGE3()
                        .setMaxInterval(30000)
                        .setMinInterval(10000),
                new FD_SOCK(),
                new FD_ALL()
                        .setTimeout(3000)
                        .setInterval(1000),
                new VERIFY_SUSPECT()
                        .setTimeout(1000),
                new NAKACK2()
                        .setUseMcastXmit(false),
                new UNICAST3()
                        .setXmitInterval(100)
                        .setXmitTableNumRows(50)
                        .setXmitTableMsgsPerRow(1024),
                new STABLE()
                        .desiredAverageGossip(2000),
                new FRAG3(),
                new GMS()
                        .printLocalAddress(false),
                new UFC(),
                new MFC()
        };


        /**
         * On crée la configuration JChannel avec notre stack
         *
         * We create the JChannel with our stack
         */
        JChannel jChannel = new JChannel(protocols).name("EXEMPLE");


        /**
         * On crée la configuration JGroups avec notre configuration JChannel
         *
         * We create the JGroups with our JChannel configuration
         */
        JGroupsTransport transport = new JGroupsTransport(jChannel);


        /**
         * Si la persistance est activée il faut en définir le chemin dans la configuration globale
         *
         * If the persitence is enabled, we must define its path in the global configuration
         */
        if (persistanceActive)
            globalConfigurationBuilder
                    .cacheContainer()
                    .globalState()
                    .enable()
                    .persistentLocation(persistanceChemin);


        /**
         * On défini la configuration du cache clusterisé
         *
         * We define the cluster cache configuration
         */
        globalConfigurationBuilder.cacheContainer()
                .name("exemple-cache")
                .transport()
                    .clusterName("exemple-cluster")
                    .machineId(machine)
                    .transport(transport)
                .serialization()
                    // ExempleObjectContextInitializerImpl existe après le mvn install du projet
                    // ExempleObjectContextInitializerImpl exists after mvn install of project
                    .addContextInitializer(new ExempleObjectContextInitializerImpl());


        /**
         * On termine la configuration
         *
         * We build the infinispan configuration
         */
        GlobalConfiguration globalConfiguration = globalConfigurationBuilder.build();


        log.info("Démarrage cache manager");


        /**
         * On retourne le CacheManager issu de la configuration globale créée dans un nouveau bean EmbeddedCacheManager
         *
         * We return the CacheManager build from the global configuration in a new bean EmbeddedCacheManager
         */
        return new DefaultCacheManager(globalConfiguration);

    }

}
