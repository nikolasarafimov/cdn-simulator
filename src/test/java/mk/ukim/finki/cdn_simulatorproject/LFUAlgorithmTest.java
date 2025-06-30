package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LFUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LFUAlgorithmTest {

    @Test
    void testLFUCacheEviction() {
        LFUAlgorithm lfu = new LFUAlgorithm(2);

        Resource r1 = new Resource("res1", "image", 100,"");
        Resource r2 = new Resource("res2", "image", 200,"");
        Resource r3 = new Resource("res3", "image", 300,"");

        lfu.putInCache(r1);
        lfu.putInCache(r2);

        lfu.getResource("res1");
        lfu.putInCache(r3);

        assertNull(lfu.getResource("res2"), "r2 should be evicted because it was the least frequently used");
        assertNotNull(lfu.getResource("res1"), "r1 should remain");
        assertNotNull(lfu.getResource("res3"), "r3 should be added");
    }
}