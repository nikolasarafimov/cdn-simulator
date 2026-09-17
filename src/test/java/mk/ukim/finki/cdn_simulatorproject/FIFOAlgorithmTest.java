package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.FIFOAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FIFOAlgorithmTest {

    @Test
    void shouldEvictOldestResourceWhenCapacityIsExceeded() {
        FIFOAlgorithm fifo = new FIFOAlgorithm(2);

        Resource resource1 =
                new Resource(
                        "r1",
                        "image",
                        100,
                        "/img/r1.jpg"
                );

        Resource resource2 =
                new Resource(
                        "r2",
                        "image",
                        200,
                        "/img/r2.jpg"
                );

        Resource resource3 =
                new Resource(
                        "r3",
                        "image",
                        300,
                        "/img/r3.jpg"
                );

        fifo.putInCache(resource1);
        fifo.putInCache(resource2);

        assertTrue(fifo.isCacheFull());

        fifo.putInCache(resource3);

        assertNull(
                fifo.getResource("r1")
        );

        assertNotNull(
                fifo.getResource("r2")
        );

        assertNotNull(
                fifo.getResource("r3")
        );
    }
}