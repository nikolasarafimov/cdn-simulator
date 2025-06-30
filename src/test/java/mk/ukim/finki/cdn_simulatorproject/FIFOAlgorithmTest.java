package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.FIFOAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FIFOAlgorithmTest {

    @Test
    void testFIFOQueueBehavior() {
        FIFOAlgorithm fifo = new FIFOAlgorithm(2);

        Resource r1 = new Resource("r1", "type", 100,"");
        Resource r2 = new Resource("r2", "type", 200,"");
        Resource r3 = new Resource("r3", "type", 300,"");

        fifo.putInCache(r1);
        fifo.putInCache(r2);
        assertTrue(fifo.isCacheFull(), "Cache should be full after inserting 2 items (capacity=2)");

        fifo.putInCache(r3);
        assertNull(fifo.getResource("r1"), "r1 should have been evicted by FIFO");
        assertNotNull(fifo.getResource("r2"), "r2 should remain");
        assertNotNull(fifo.getResource("r3"), "r3 should be added");
    }
}