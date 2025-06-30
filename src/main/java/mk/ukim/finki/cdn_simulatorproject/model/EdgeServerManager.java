package mk.ukim.finki.cdn_simulatorproject.model;

import lombok.Data;
import mk.ukim.finki.cdn_simulatorproject.dto.HopDTO;
import mk.ukim.finki.cdn_simulatorproject.exceptions.EdgeServerException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@Service
public class EdgeServerManager {

    private final List<EdgeServer> edgeServerList;

    public EdgeServerManager() {
        this.edgeServerList = new ArrayList<>();
    }

    public void addEdgeServer(EdgeServer edgeServer){
        edgeServerList.add(edgeServer);
    }

    public Resource routeRequest(String resId, List<HopDTO> trace){
        EdgeServer edge = getTheLeastLoadedReplicaServer();
        ClientRequest cr = new ClientRequest();
        cr.setResourceId(resId);
        return edge.handleRequest(cr, trace);
    }

    public Resource routeRequest(String resId){
        return routeRequest(resId, new ArrayList<>());
    }

    public void removeEdgeServer(EdgeServer edgeServer){
        edgeServerList.remove(edgeServer);
    }

    public EdgeServer getTheLeastLoadedReplicaServer(){
        return edgeServerList.stream()
                .min(Comparator.comparingInt(EdgeServer::getRequestCount))
                .orElseThrow(EdgeServerException::new);
    }
    public List<EdgeServer> getEdgeServerList() {
        return edgeServerList;
    }
}
