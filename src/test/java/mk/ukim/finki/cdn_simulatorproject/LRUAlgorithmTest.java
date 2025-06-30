package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LRUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LRUAlgorithmTest {

    @Test
    void testLRUCacheInsertion() {
        LRUAlgorithm lru = new LRUAlgorithm(2);

        Resource r1 = new Resource("res1", "image", 100,"");
        lru.putInCache(r1);
        assertNotNull(lru.getResource("res1"), "res1 should be in the cache");

        Resource r2 = new Resource("res2", "image", 200,"");
        lru.putInCache(r2);
        assertNotNull(lru.getResource("res2"), "res2 should be in the cache");

        Resource r3 = new Resource("res3", "video", 300,"");
        lru.putInCache(r3);

        assertNull(lru.getResource("res1"), "res1 should have been evicted by LRU");
        assertNotNull(lru.getResource("res2"), "res2 should still be in cache");
        assertNotNull(lru.getResource("res3"), "res3 should be in the cache");
    }

    @Test
    void testLRUCacheHitRefreshesOrder() {
        LRUAlgorithm lru = new LRUAlgorithm(2);

        Resource r1 = new Resource("res1", "image", 100,"");
        Resource r2 = new Resource("res2", "image", 200,"");
        lru.putInCache(r1);
        lru.putInCache(r2);

        assertNotNull(lru.getResource("res1"), "res1 should be found in the cache");
        Resource r3 = new Resource("res3", "video", 300,"");
        lru.putInCache(r3);
        assertNotNull(lru.getResource("res1"), "res1 should still be in the cache because it was just accessed");
        assertNull(lru.getResource("res2"), "res2 should have been evicted");
        assertNotNull(lru.getResource("res3"), "res3 should be in the cache");
    }
}