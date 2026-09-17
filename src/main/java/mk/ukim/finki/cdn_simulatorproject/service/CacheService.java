package mk.ukim.finki.cdn_simulatorproject.service;

import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;

public interface CacheService {

    Resource fetchResource(String resourceId);

    void clearCache();

    void addReplicaServer(ReplicaServer replicaServer);

    void removeReplicaServer(ReplicaServer replicaServer);
}