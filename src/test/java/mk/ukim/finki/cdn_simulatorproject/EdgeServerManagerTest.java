package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LRUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServer;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServerManager;
import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EdgeServerManagerTest {

    @Test
    void shouldReturnLeastLoadedEdgeServer() {
        EdgeServerManager manager = new EdgeServerManager();
        OriginServer originServer = new OriginServer();

        ReplicaServer replica1 = new ReplicaServer(
                "Replica1",
                new LRUAlgorithm(2),
                originServer,
                "us-east-1"
        );

        ReplicaServer replica2 = new ReplicaServer(
                "Replica2",
                new LRUAlgorithm(2),
                originServer,
                "eu-west-1"
        );

        ReplicaServer replica3 = new ReplicaServer(
                "Replica3",
                new LRUAlgorithm(2),
                originServer,
                "eu-east-1"
        );

        EdgeServer edge1 = new EdgeServer(
                "Edge1",
                new LRUAlgorithm(2),
                replica1
        );

        EdgeServer edge2 = new EdgeServer(
                "Edge2",
                new LRUAlgorithm(2),
                replica2
        );

        EdgeServer edge3 = new EdgeServer(
                "Edge3",
                new LRUAlgorithm(2),
                replica3
        );

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
}