package mk.ukim.finki.cdn_simulatorproject.service;

import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;

public interface CacheService {

    void clearCache();

    void addReplicaServer(ReplicaServer replicaServer);

    void removeReplicaServer(ReplicaServer replicaServer);
}