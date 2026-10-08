package mk.ukim.finki.cdn_simulatorproject.model;

import mk.ukim.finki.cdn_simulatorproject.dto.HopDTO;
import mk.ukim.finki.cdn_simulatorproject.exceptions.EdgeServerException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class EdgeServerManager {

    private final List<EdgeServer> edgeServerList;

    public EdgeServerManager() {
        this.edgeServerList = new CopyOnWriteArrayList<>();
    }

    public synchronized void addEdgeServer(
            EdgeServer edgeServer
    ) {
        if (edgeServer == null) {
            throw new IllegalArgumentException(
                    "Edge server is required."
            );
        }

        boolean alreadyExists =
                edgeServerList.stream()
                        .anyMatch(existing ->
                                Objects.equals(
                                        existing.getEdgeServerId(),
                                        edgeServer.getEdgeServerId()
                                )
                        );

        if (!alreadyExists) {
            edgeServerList.add(edgeServer);
        }
    }

    public Resource routeRequest(
            String resourceId,
            List<HopDTO> trace
    ) {
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException(
                    "Resource ID is required."
            );
        }

        if (trace == null) {
            throw new IllegalArgumentException(
                    "Request trace is required."
            );
        }

        EdgeServer edgeServer =
                getLeastLoadedEdgeServer();

        ClientRequest clientRequest =
                new ClientRequest();

        clientRequest.setResourceId(resourceId);

        return edgeServer.handleRequest(
                clientRequest,
                trace
        );
    }

    public Resource routeRequest(
            String resourceId
    ) {
        return routeRequest(
                resourceId,
                new ArrayList<>()
        );
    }

    public synchronized void removeEdgeServer(
            EdgeServer edgeServer
    ) {
        if (edgeServer == null) {
            return;
        }

        edgeServerList.removeIf(existing ->
                Objects.equals(
                        existing.getEdgeServerId(),
                        edgeServer.getEdgeServerId()
                )
        );
    }

    public EdgeServer getLeastLoadedEdgeServer() {
        return edgeServerList.stream()
                .min(
                        Comparator
                                .comparingInt(
                                        EdgeServer::getRequestCount
                                )
                                .thenComparing(
                                        EdgeServer::getEdgeServerId
                                )
                )
                .orElseThrow(
                        EdgeServerException::new
                );
    }

    public List<EdgeServer> getEdgeServerList() {
        return List.copyOf(edgeServerList);
    }
}