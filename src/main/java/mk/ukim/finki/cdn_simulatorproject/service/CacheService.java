package mk.ukim.finki.cdn_simulatorproject.service;

import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;

public interface CacheService {
    public Resource fetchResource(String resourceId);
    public void clearCache();

    public void addReplicaServer(ReplicaServer replicaServer);
    public void removeReplicaServer(ReplicaServer replicaServer);
}
