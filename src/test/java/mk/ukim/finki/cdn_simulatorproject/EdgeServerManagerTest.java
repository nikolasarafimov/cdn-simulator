package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LRUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.exceptions.EdgeServerException;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServer;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServerManager;
import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EdgeServerManagerTest {

    @Test
    void shouldReturnLeastLoadedEdgeServer() {
        EdgeServerManager manager = new EdgeServerManager();

        EdgeServer edge1 = createEdgeServer("Edge1");
        EdgeServer edge2 = createEdgeServer("Edge2");
        EdgeServer edge3 = createEdgeServer("Edge3");

        manager.addEdgeServer(edge1);
        manager.addEdgeServer(edge2);
        manager.addEdgeServer(edge3);

        edge1.setRequestCount(10);
        edge2.setRequestCount(2);
        edge3.setRequestCount(5);

        EdgeServer leastLoaded =
                manager.getLeastLoadedEdgeServer();

        assertEquals(
                "Edge2",
                leastLoaded.getEdgeServerId()
        );
    }

    @Test
    void shouldUseServerIdAsTieBreaker() {
        EdgeServerManager manager = new EdgeServerManager();

        EdgeServer edgeB = createEdgeServer("EdgeB");
        EdgeServer edgeA = createEdgeServer("EdgeA");

        edgeB.setRequestCount(3);
        edgeA.setRequestCount(3);

        manager.addEdgeServer(edgeB);
        manager.addEdgeServer(edgeA);

        EdgeServer leastLoaded =
                manager.getLeastLoadedEdgeServer();

        assertEquals(
                "EdgeA",
                leastLoaded.getEdgeServerId()
        );
    }

    @Test
    void shouldIgnoreDuplicateEdgeServerId() {
        EdgeServerManager manager = new EdgeServerManager();

        manager.addEdgeServer(
                createEdgeServer("Edge1")
        );

        manager.addEdgeServer(
                createEdgeServer("Edge1")
        );

        assertEquals(
                1,
                manager.getEdgeServerList().size()
        );
    }

    @Test
    void shouldThrowWhenNoEdgeServersExist() {
        EdgeServerManager manager =
                new EdgeServerManager();

        assertThrows(
                EdgeServerException.class,
                manager::getLeastLoadedEdgeServer
        );
    }

    private EdgeServer createEdgeServer(
            String edgeServerId
    ) {
        OriginServer originServer =
                new OriginServer();

        ReplicaServer replicaServer =
                new ReplicaServer(
                        "Replica-" + edgeServerId,
                        new LRUAlgorithm(2),
                        originServer,
                        "test-region"
                );

        return new EdgeServer(
                edgeServerId,
                new LRUAlgorithm(2),
                replicaServer
        );
    }
}