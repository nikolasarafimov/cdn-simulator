package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LRUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServer;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServerManager;
import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EdgeServerManagerTest {

    @Test
    void testGetTheLeastLoadedReplicaServer() {
        EdgeServerManager manager = new EdgeServerManager();

        OriginServer origin = new OriginServer();
        LRUAlgorithm lru = new LRUAlgorithm(2);

        EdgeServer e1 = new EdgeServer("Edge1", lru, new ReplicaServer("Replica1", lru, origin,"us-east-1"));
        EdgeServer e2 = new EdgeServer("Edge2", lru, new ReplicaServer("Replica2", lru, origin,"eu-west-1"));
        EdgeServer e3 = new EdgeServer("Edge3", lru, new ReplicaServer("Replica3", lru, origin,"eu-east-1"));

        manager.addEdgeServer(e1);
        manager.addEdgeServer(e2);
        manager.addEdgeServer(e3);

        e1.setRequestCount(10);
        e2.setRequestCount(2);
        e3.setRequestCount(5);

        EdgeServer leastLoaded = manager.getTheLeastLoadedReplicaServer();
        assertEquals("Edge2", leastLoaded.getEdgeServerId());
    }
}