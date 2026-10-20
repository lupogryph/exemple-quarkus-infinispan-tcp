# Quarkus infinispan cluster project template

Sample quarkus application project for creating apps with can share their cache through TCP layer (with jgroups)

JDK 11

Launch multiple instances to simulate a cluster with maven

N1 : `quarkus:dev -Dquarkus.http.port=1080 -Dinfinispan.port=1500 -Dinfinispan.machine=N1 -Dinfinispan.members=localhost[1500],localhost[2500] -Dinfinispan.persistance.chemin=infinispan/n1`

N2 : `quarkus:dev -Dquarkus.http.port=2080 -Dinfinispan.port=2500 -Dinfinispan.machine=N2 -Dinfinispan.members=localhost[1500],localhost[2500] -Dinfinispan.persistance.chemin=infinispan/n2`
