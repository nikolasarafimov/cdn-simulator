package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OriginServerTest {

    @Test
    void shouldReturnExistingResource() {
        OriginServer originServer = new OriginServer();

        Resource resource =
                originServer.getResourceFromOriginServer("img1");

        assertNotNull(resource);

        assertEquals(
                "img1",
                resource.getResourceId()
        );

        assertEquals(
                "/img/bali.jpg",
                resource.getResourcePath()
        );
    }

    @Test
    void shouldReturnNullForUnknownResource() {
        OriginServer originServer = new OriginServer();

        Resource resource =
                originServer.getResourceFromOriginServer("unknown");

        assertNull(resource);
    }
}