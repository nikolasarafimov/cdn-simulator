package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.FIFOAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FIFOAlgorithmTest {

    @Test
    void shouldEvictOldestResourceWhenCapacityIsExceeded() {
        FIFOAlgorithm fifo = new FIFOAlgorithm(2);

        Resource resource1 = createResource("r1");
        Resource resource2 = createResource("r2");
        Resource resource3 = createResource("r3");

        fifo.putInCache(resource1);
        fifo.putInCache(resource2);

        assertTrue(fifo.isCacheFull());

        fifo.putInCache(resource3);

        assertNull(fifo.getResource("r1"));
        assertNotNull(fifo.getResource("r2"));
        assertNotNull(fifo.getResource("r3"));
    }

    @Test
    void shouldPreserveInsertionOrderInSnapshot() {
        FIFOAlgorithm fifo = new FIFOAlgorithm(3);

        fifo.putInCache(createResource("r1"));
        fifo.putInCache(createResource("r2"));
        fifo.putInCache(createResource("r3"));

        assertEquals(
                java.util.List.of("r1", "r2", "r3"),
                fifo.snapshot()
                        .stream()
                        .map(Resource::getResourceId)
                        .toList()
        );
    }

    @Test
    void shouldIgnoreDuplicateResourceInsertion() {
        FIFOAlgorithm fifo = new FIFOAlgorithm(2);

        Resource resource1 = createResource("r1");

        fifo.putInCache(resource1);
        fifo.putInCache(resource1);

        assertEquals(1, fifo.snapshot().size());
        assertNotNull(fifo.getResource("r1"));
    }

    @Test
    void shouldClearCache() {
        FIFOAlgorithm fifo = new FIFOAlgorithm(2);

        fifo.putInCache(createResource("r1"));
        fifo.putInCache(createResource("r2"));

        fifo.clearCache();

        assertTrue(fifo.snapshot().isEmpty());
        assertFalse(fifo.isCacheFull());
    }

    @Test
    void shouldRejectNonPositiveCapacity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FIFOAlgorithm(0)
        );
    }

    private Resource createResource(String id) {
        return new Resource(
                id,
                "image",
                100,
                "/img/" + id + ".jpg"
        );
    }
}