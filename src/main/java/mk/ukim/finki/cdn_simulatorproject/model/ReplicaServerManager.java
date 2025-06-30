package mk.ukim.finki.cdn_simulatorproject.model;

import lombok.Data;
import mk.ukim.finki.cdn_simulatorproject.exceptions.ReplicaServerException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@Service
public class ReplicaServerManager {

    private final List<ReplicaServer> replicaServerList;

    public ReplicaServerManager(){
        this.replicaServerList = new ArrayList<>();
    }
    public ReplicaServerManager(List<ReplicaServer> replicaServerList) {
        this.replicaServerList = new ArrayList<>();
    }

    public void addReplicaServer(ReplicaServer replicaServer){
        replicaServerList.add(replicaServer);
    }

    public void removeReplicaServer(ReplicaServer replicaServer){
        replicaServerList.remove(replicaServer);
    }

    public ReplicaServer getTheLeastLoadedReplicaServer(){
           return replicaServerList.stream()
                    .min(Comparator.comparingInt(ReplicaServer::getCountRequests))
                   .orElseThrow(ReplicaServerException::new);
    }

    public List<ReplicaServer> getReplicaServerList() {
        return replicaServerList;
    }
}
