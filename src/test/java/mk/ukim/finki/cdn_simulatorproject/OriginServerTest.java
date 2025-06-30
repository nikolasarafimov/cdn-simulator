package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.model.OriginServer;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OriginServerTest {

    @Test
    void testGetResourceFromOriginServer() {
        OriginServer originServer = new OriginServer();
        Resource resource = originServer.getResourceFromOriginServer("resourceNum1");
        assertNotNull(resource, "resourceNum1 is predefined, it should be found");
        assertEquals("resourceNum1", resource.getResourceId());
    }

}