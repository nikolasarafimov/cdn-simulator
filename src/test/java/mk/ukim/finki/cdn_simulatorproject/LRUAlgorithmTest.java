package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LRUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LRUAlgorithmTest {

    @Test
    void shouldEvictLeastRecentlyUsedResource() {
        LRUAlgorithm lru = new LRUAlgorithm(2);

        Resource resource1 = createResource("res1");
        Resource resource2 = createResource("res2");
        Resource resource3 = createResource("res3");

        lru.putInCache(resource1);
        lru.putInCache(resource2);
        lru.putInCache(resource3);

        assertNull(lru.getResource("res1"));
        assertNotNull(lru.getResource("res2"));
        assertNotNull(lru.getResource("res3"));
    }

    @Test
    void shouldRefreshAccessOrderOnCacheHit() {
        LRUAlgorithm lru = new LRUAlgorithm(2);

        Resource resource1 = createResource("res1");
        Resource resource2 = createResource("res2");
        Resource resource3 = createResource("res3");

        lru.putInCache(resource1);
        lru.putInCache(resource2);

        assertNotNull(lru.getResource("res1"));

        lru.putInCache(resource3);

        assertNotNull(lru.getResource("res1"));
        assertNull(lru.getResource("res2"));
        assertNotNull(lru.getResource("res3"));
    }

    @Test
    void shouldNotRefreshAccessOrderOnDuplicateInsertion() {
        LRUAlgorithm lru = new LRUAlgorithm(2);

        Resource resource1 = createResource("res1");
        Resource resource2 = createResource("res2");

        lru.putInCache(resource1);
        lru.putInCache(resource2);

        lru.putInCache(resource1);

        lru.putInCache(createResource("res3"));

        assertNull(lru.getResource("res1"));
        assertNotNull(lru.getResource("res2"));
        assertNotNull(lru.getResource("res3"));
    }

    @Test
    void shouldIgnoreDuplicateResourceInsertion() {
        LRUAlgorithm lru = new LRUAlgorithm(2);

        Resource resource = createResource("res1");

        lru.putInCache(resource);
        lru.putInCache(resource);

        assertEquals(1, lru.snapshot().size());
    }

    @Test
    void shouldClearCache() {
        LRUAlgorithm lru = new LRUAlgorithm(2);

        lru.putInCache(createResource("res1"));
        lru.putInCache(createResource("res2"));

        assertTrue(lru.isCacheFull());

        lru.clearCache();

        assertTrue(lru.snapshot().isEmpty());
        assertFalse(lru.isCacheFull());
    }

    @Test
    void shouldRejectNonPositiveCapacity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LRUAlgorithm(0)
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