package mk.ukim.finki.cdn_simulatorproject.service;

import mk.ukim.finki.cdn_simulatorproject.dto.HopDTO;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;

import java.util.List;

public interface CDNService {
    Resource fetchResource(String resourceId,List<HopDTO> trace);
    Resource fetchResource(String resourceId);
    void addReplicaServer(ReplicaServer replicaServer);
    void removeReplicaServer(ReplicaServer replicaServer);
    void clearCache();
}
