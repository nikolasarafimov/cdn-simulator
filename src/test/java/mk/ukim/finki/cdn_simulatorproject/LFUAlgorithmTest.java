package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LFUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LFUAlgorithmTest {

    @Test
    void shouldEvictLeastFrequentlyUsedResource() {
        LFUAlgorithm lfu = new LFUAlgorithm(2);

        Resource resource1 = createResource("res1");
        Resource resource2 = createResource("res2");
        Resource resource3 = createResource("res3");

        lfu.putInCache(resource1);
        lfu.putInCache(resource2);

        lfu.getResource("res1");

        lfu.putInCache(resource3);

        assertNull(lfu.getResource("res2"));
        assertNotNull(lfu.getResource("res1"));
        assertNotNull(lfu.getResource("res3"));
    }

    @Test
    void shouldEvictOldestResourceWhenFrequenciesAreEqual() {
        LFUAlgorithm lfu = new LFUAlgorithm(2);

        lfu.putInCache(createResource("res1"));
        lfu.putInCache(createResource("res2"));

        lfu.putInCache(createResource("res3"));

        assertNull(lfu.getResource("res1"));
        assertNotNull(lfu.getResource("res2"));
        assertNotNull(lfu.getResource("res3"));
    }

    @Test
    void shouldIncreaseFrequencyOnlyWhenResourceIsAccessed() {
        LFUAlgorithm lfu = new LFUAlgorithm(2);

        Resource resource1 = createResource("res1");
        Resource resource2 = createResource("res2");

        lfu.putInCache(resource1);
        lfu.putInCache(resource2);

        lfu.putInCache(resource1);

        lfu.putInCache(createResource("res3"));

        assertNull(lfu.getResource("res1"));
        assertNotNull(lfu.getResource("res2"));
        assertNotNull(lfu.getResource("res3"));
    }

    @Test
    void shouldIgnoreDuplicateResourceInsertion() {
        LFUAlgorithm lfu = new LFUAlgorithm(2);

        Resource resource = createResource("res1");

        lfu.putInCache(resource);
        lfu.putInCache(resource);

        assertEquals(1, lfu.snapshot().size());
    }

    @Test
    void shouldClearCache() {
        LFUAlgorithm lfu = new LFUAlgorithm(2);

        lfu.putInCache(createResource("res1"));
        lfu.putInCache(createResource("res2"));

        assertTrue(lfu.isCacheFull());

        lfu.clearCache();

        assertTrue(lfu.snapshot().isEmpty());
        assertFalse(lfu.isCacheFull());
    }

    @Test
    void shouldRejectNonPositiveCapacity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LFUAlgorithm(0)
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