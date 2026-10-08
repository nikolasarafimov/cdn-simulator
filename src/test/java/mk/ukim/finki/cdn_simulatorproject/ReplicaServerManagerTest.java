package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LRUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.exceptions.ReplicaServerException;
import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServerManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReplicaServerManagerTest {

    @Test
    void shouldReturnLeastLoadedReplicaServer() {
        ReplicaServerManager manager = new ReplicaServerManager();

        ReplicaServer replica1 = createReplicaServer("Replica1");
        ReplicaServer replica2 = createReplicaServer("Replica2");
        ReplicaServer replica3 = createReplicaServer("Replica3");

        manager.addReplicaServer(replica1);
        manager.addReplicaServer(replica2);
        manager.addReplicaServer(replica3);

        replica1.setCountRequests(8);
        replica2.setCountRequests(2);
        replica3.setCountRequests(5);

        ReplicaServer leastLoaded =
                manager.getTheLeastLoadedReplicaServer();

        assertEquals(
                "Replica2",
                leastLoaded.getReplicaServerId()
        );
    }

    @Test
    void shouldUseServerIdAsTieBreaker() {
        ReplicaServerManager manager = new ReplicaServerManager();

        ReplicaServer replicaB = createReplicaServer("ReplicaB");
        ReplicaServer replicaA = createReplicaServer("ReplicaA");

        replicaB.setCountRequests(3);
        replicaA.setCountRequests(3);

        manager.addReplicaServer(replicaB);
        manager.addReplicaServer(replicaA);

        ReplicaServer leastLoaded =
                manager.getTheLeastLoadedReplicaServer();

        assertEquals(
                "ReplicaA",
                leastLoaded.getReplicaServerId()
        );
    }

    @Test
    void shouldIgnoreDuplicateReplicaServerId() {
        ReplicaServerManager manager =
                new ReplicaServerManager();

        manager.addReplicaServer(
                createReplicaServer("Replica1")
        );

        manager.addReplicaServer(
                createReplicaServer("Replica1")
        );

        assertEquals(
                1,
                manager.getReplicaServerList().size()
        );
    }

    @Test
    void shouldThrowWhenNoReplicaServersExist() {
        ReplicaServerManager manager =
                new ReplicaServerManager();

        assertThrows(
                ReplicaServerException.class,
                manager::getTheLeastLoadedReplicaServer
        );
    }

    @Test
    void shouldRemoveReplicaServer() {
        ReplicaServerManager manager = new ReplicaServerManager();

        ReplicaServer replica1 =
                createReplicaServer("Replica1");

        ReplicaServer replica2 =
                createReplicaServer("Replica2");

        manager.addReplicaServer(replica1);
        manager.addReplicaServer(replica2);

        manager.removeReplicaServer(replica1);

        assertEquals(
                1,
                manager.getReplicaServerList().size()
        );

        assertEquals(
                "Replica2",
                manager.getReplicaServerList()
                        .getFirst()
                        .getReplicaServerId()
        );
    }

    private ReplicaServer createReplicaServer(
            String replicaServerId
    ) {
        return new ReplicaServer(
                replicaServerId,
                new LRUAlgorithm(2),
                new OriginServer(),
                "test-region"
        );
    }
}