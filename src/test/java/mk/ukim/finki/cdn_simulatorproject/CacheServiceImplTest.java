package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.FIFOAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LRUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServer;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServerManager;
import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServerManager;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import mk.ukim.finki.cdn_simulatorproject.service.impl.CacheServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheServiceImplTest {

    @Test
    void shouldClearAllEdgeAndReplicaCaches() {
        EdgeServerManager edgeServerManager =
                new EdgeServerManager();

        ReplicaServerManager replicaServerManager =
                new ReplicaServerManager();

        OriginServer originServer =
                new OriginServer();

        ReplicaServer replicaServer =
                new ReplicaServer(
                        "replica-1",
                        new LRUAlgorithm(2),
                        originServer,
                        "test-region"
                );

        EdgeServer edgeServer =
                new EdgeServer(
                        "edge-1",
                        new FIFOAlgorithm(2),
                        replicaServer
                );

        replicaServerManager.addReplicaServer(replicaServer);
        edgeServerManager.addEdgeServer(edgeServer);

        Resource resource = new Resource(
                "r1",
                "image",
                100,
                "/img/r1.jpg"
        );

        edgeServer.getCacheStrategy()
                .putInCache(resource);

        replicaServer.getCacheStrategy()
                .putInCache(resource);

        CacheServiceImpl cacheService =
                new CacheServiceImpl(
                        edgeServerManager,
                        replicaServerManager
                );

        cacheService.clearCache();

        assertTrue(
                edgeServer.getCacheStrategy()
                        .snapshot()
                        .isEmpty()
        );

        assertTrue(
                replicaServer.getCacheStrategy()
                        .snapshot()
                        .isEmpty()
        );
    }

    @Test
    void shouldAddReplicaServer() {
        EdgeServerManager edgeServerManager =
                new EdgeServerManager();

        ReplicaServerManager replicaServerManager =
                new ReplicaServerManager();

        CacheServiceImpl cacheService =
                new CacheServiceImpl(
                        edgeServerManager,
                        replicaServerManager
                );

        ReplicaServer replicaServer =
                new ReplicaServer(
                        "replica-1",
                        new LRUAlgorithm(2),
                        new OriginServer(),
                        "test-region"
                );

        cacheService.addReplicaServer(replicaServer);

        assertEquals(
                1,
                replicaServerManager
                        .getReplicaServerList()
                        .size()
        );

        assertEquals(
                "replica-1",
                replicaServerManager
                        .getReplicaServerList()
                        .getFirst()
                        .getReplicaServerId()
        );
    }

    @Test
    void shouldRemoveReplicaServer() {
        EdgeServerManager edgeServerManager =
                new EdgeServerManager();

        ReplicaServerManager replicaServerManager =
                new ReplicaServerManager();

        CacheServiceImpl cacheService =
                new CacheServiceImpl(
                        edgeServerManager,
                        replicaServerManager
                );

        ReplicaServer replicaServer =
                new ReplicaServer(
                        "replica-1",
                        new LRUAlgorithm(2),
                        new OriginServer(),
                        "test-region"
                );

        cacheService.addReplicaServer(replicaServer);
        cacheService.removeReplicaServer(replicaServer);

        assertTrue(
                replicaServerManager
                        .getReplicaServerList()
                        .isEmpty()
        );
    }
}