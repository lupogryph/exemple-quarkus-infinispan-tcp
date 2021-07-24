package lupogryph.exemple.quarkus;

import io.quarkus.runtime.Startup;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

@ApplicationScoped
@Startup
@Slf4j
public class ExempleObjectCacheService {

    public static final String EXEMPLE_CACHE = "exemple_cache";

    @ConfigProperty(name = "infinispan.persistance.active")
    public Boolean persistanceActive;

    @Inject
    EmbeddedCacheManager cacheManager;

    private Cache<Integer, ExempleObject> cache;

    /**
     * Initialise le cache 'exemple' dans le cluster
     * Le cache est en mode REPL_SYNC ce qui veut dire que chaque action (ajout, suppression, modification) d'une entrée
     * et répliquée sur les autres membres du cluster
     * La lecture des entrées du cache est faite localement
     *
     * Initiaize the 'exemple' cache in cluster
     * The cache is in REPL_SYNC mode, which means that every action (add, delete, update) of an entry is replicated
     * on the others members of this cluster
     * But the cache's entries reading is done locally
     */
    @PostConstruct
    public void init() {
        log.info("Initialisation cache {}...", EXEMPLE_CACHE);
        ConfigurationBuilder configuration = new ConfigurationBuilder();
        configuration
                .clustering()
                .cacheMode(CacheMode.REPL_ASYNC)
        ;
        if (persistanceActive)
            configuration
                    .persistence()
                    .addSingleFileStore()
                    .location(EXEMPLE_CACHE)
                    .maxEntries(1000)
                    ;
        cache = cacheManager.createCache(EXEMPLE_CACHE, configuration.build());
    }

    public ExempleObject put(ExempleObject exempleObject){
        return cache.put(exempleObject.hashCode(), exempleObject);
    }

    public Collection<ExempleObject> get() {
        return cache.values();
    }

    public ExempleObject get(Integer key){
        return cache.get(key);
    }

    public ExempleObject findByNom(String nom) {
        return cache.values().stream()
                .filter(exempleObject -> nom.equals(exempleObject.getNom()))
                .findFirst()
                .orElseThrow();
    }

    public void delete(Integer key) {
        cache.remove(key);
    }

    public ExempleObject update(Integer key, ExempleObject exempleObject) {
        return cache.replace(key, exempleObject);
    }

    public List<Address> status() {
        return cacheManager.getMembers();
    }

}
