package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OriginServerTest {

    @Test
    void shouldReturnExistingResource() {
        OriginServer originServer = new OriginServer();

        Resource resource =
                originServer.getResourceFromOriginServer("img1");

        assertNotNull(resource);
        assertEquals("img1", resource.getResourceId());
        assertEquals("image", resource.getResourceType());
        assertEquals("/img/bali.jpg", resource.getResourcePath());
    }

    @Test
    void shouldReturnNullForUnknownResource() {
        OriginServer originServer = new OriginServer();

        Resource resource =
                originServer.getResourceFromOriginServer("unknown");

        assertNull(resource);
    }

    @Test
    void shouldReturnNullForInvalidResourceId() {
        OriginServer originServer = new OriginServer();

        assertNull(
                originServer.getResourceFromOriginServer(null)
        );

        assertNull(
                originServer.getResourceFromOriginServer("")
        );

        assertNull(
                originServer.getResourceFromOriginServer("   ")
        );
    }

    @Test
    void shouldExposeAllPredefinedResources() {
        OriginServer originServer = new OriginServer();

        List<Resource> resources =
                originServer.listAll();

        assertEquals(6, resources.size());

        assertEquals(
                List.of(
                        "img1",
                        "img2",
                        "img3",
                        "img4",
                        "img5",
                        "img6"
                ),
                resources.stream()
                        .map(Resource::getResourceId)
                        .toList()
        );

        assertTrue(
                resources.stream()
                        .allMatch(resource ->
                                resource.getResourcePath() != null
                                        && !resource.getResourcePath().isBlank()
                        )
        );
    }
}