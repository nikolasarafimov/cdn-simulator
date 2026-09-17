package mk.ukim.finki.cdn_simulatorproject.model;

import mk.ukim.finki.cdn_simulatorproject.exceptions.ReplicaServerException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReplicaServerManager {

    private final List<ReplicaServer> replicaServerList;

    public ReplicaServerManager() {
        this.replicaServerList = new ArrayList<>();
    }

    public ReplicaServerManager(List<ReplicaServer> replicaServerList) {
        if (replicaServerList == null) {
            throw new IllegalArgumentException("Replica server list is required.");
        }

        this.replicaServerList = new ArrayList<>(replicaServerList);
    }

    public void addReplicaServer(ReplicaServer replicaServer) {
        if (replicaServer == null) {
            throw new IllegalArgumentException("Replica server is required.");
        }

        boolean alreadyExists = replicaServerList.stream()
                .anyMatch(existing ->
                        existing.getReplicaServerId()
                                .equals(replicaServer.getReplicaServerId()));

        if (!alreadyExists) {
            replicaServerList.add(replicaServer);
        }
    }

    public void removeReplicaServer(ReplicaServer replicaServer) {
        if (replicaServer == null) {
            return;
        }

        replicaServerList.remove(replicaServer);
    }

    public ReplicaServer getTheLeastLoadedReplicaServer() {
        return replicaServerList.stream()
                .min(
                        Comparator
                                .comparingInt(ReplicaServer::getCountRequests)
                                .thenComparing(ReplicaServer::getReplicaServerId)
                )
                .orElseThrow(ReplicaServerException::new);
    }

    public List<ReplicaServer> getReplicaServerList() {
        return List.copyOf(replicaServerList);
    }
}