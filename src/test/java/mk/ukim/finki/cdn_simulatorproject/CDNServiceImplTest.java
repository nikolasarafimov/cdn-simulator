package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.dto.HopDTO;
import mk.ukim.finki.cdn_simulatorproject.model.EdgeServerManager;
import mk.ukim.finki.cdn_simulatorproject.model.ReplicaServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import mk.ukim.finki.cdn_simulatorproject.service.CacheService;
import mk.ukim.finki.cdn_simulatorproject.service.impl.CDNServiceImpl;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CDNServiceImplTest {

    @Test
    void shouldRouteResourceRequestThroughEdgeServerManager() {
        EdgeServerManager edgeServerManager =
                mock(EdgeServerManager.class);

        CacheService cacheService =
                mock(CacheService.class);

        CDNServiceImpl cdnService =
                new CDNServiceImpl(
                        edgeServerManager,
                        cacheService
                );

        Resource expectedResource =
                new Resource(
                        "img1",
                        "image",
                        1024,
                        "/img/bali.jpg"
                );

        List<HopDTO> trace =
                new ArrayList<>();

        when(
                edgeServerManager.routeRequest(
                        "img1",
                        trace
                )
        ).thenReturn(expectedResource);

        Resource result =
                cdnService.fetchResource(
                        "img1",
                        trace
                );

        assertSame(
                expectedResource,
                result
        );

        verify(edgeServerManager)
                .routeRequest(
                        "img1",
                        trace
                );
    }

    @Test
    void shouldFetchResourceWithoutExplicitTrace() {
        EdgeServerManager edgeServerManager =
                mock(EdgeServerManager.class);

        CacheService cacheService =
                mock(CacheService.class);

        CDNServiceImpl cdnService =
                new CDNServiceImpl(
                        edgeServerManager,
                        cacheService
                );

        Resource expectedResource =
                new Resource(
                        "img1",
                        "image",
                        1024,
                        "/img/bali.jpg"
                );

        when(
                edgeServerManager.routeRequest(
                        eq("img1"),
                        anyList()
                )
        ).thenReturn(expectedResource);

        Resource result =
                cdnService.fetchResource("img1");

        assertSame(
                expectedResource,
                result
        );

        verify(edgeServerManager)
                .routeRequest(
                        eq("img1"),
                        anyList()
                );
    }

    @Test
    void shouldDelegateReplicaAdditionToCacheService() {
        EdgeServerManager edgeServerManager =
                mock(EdgeServerManager.class);

        CacheService cacheService =
                mock(CacheService.class);

        CDNServiceImpl cdnService =
                new CDNServiceImpl(
                        edgeServerManager,
                        cacheService
                );

        ReplicaServer replicaServer =
                mock(ReplicaServer.class);

        cdnService.addReplicaServer(
                replicaServer
        );

        verify(cacheService)
                .addReplicaServer(
                        replicaServer
                );
    }

    @Test
    void shouldDelegateReplicaRemovalToCacheService() {
        EdgeServerManager edgeServerManager =
                mock(EdgeServerManager.class);

        CacheService cacheService =
                mock(CacheService.class);

        CDNServiceImpl cdnService =
                new CDNServiceImpl(
                        edgeServerManager,
                        cacheService
                );

        ReplicaServer replicaServer =
                mock(ReplicaServer.class);

        cdnService.removeReplicaServer(
                replicaServer
        );

        verify(cacheService)
                .removeReplicaServer(
                        replicaServer
                );
    }

    @Test
    void shouldDelegateCacheClearingToCacheService() {
        EdgeServerManager edgeServerManager =
                mock(EdgeServerManager.class);

        CacheService cacheService =
                mock(CacheService.class);

        CDNServiceImpl cdnService =
                new CDNServiceImpl(
                        edgeServerManager,
                        cacheService
                );

        cdnService.clearCache();

        verify(cacheService)
                .clearCache();
    }

    @Test
    void shouldRejectBlankResourceId() {
        EdgeServerManager edgeServerManager =
                mock(EdgeServerManager.class);

        CacheService cacheService =
                mock(CacheService.class);

        CDNServiceImpl cdnService =
                new CDNServiceImpl(
                        edgeServerManager,
                        cacheService
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> cdnService.fetchResource(
                        "",
                        new ArrayList<>()
                )
        );
    }

    @Test
    void shouldRejectNullTrace() {
        EdgeServerManager edgeServerManager =
                mock(EdgeServerManager.class);

        CacheService cacheService =
                mock(CacheService.class);

        CDNServiceImpl cdnService =
                new CDNServiceImpl(
                        edgeServerManager,
                        cacheService
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> cdnService.fetchResource(
                        "img1",
                        null
                )
        );
    }
}