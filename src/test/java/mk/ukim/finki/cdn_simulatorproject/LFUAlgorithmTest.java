package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LFUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class LFUAlgorithmTest {

    @Test
    void shouldEvictLeastFrequentlyUsedResource() {
        LFUAlgorithm lfu = new LFUAlgorithm(2);

        Resource resource1 =
                new Resource(
                        "res1",
                        "image",
                        100,
                        "/img/res1.jpg"
                );

        Resource resource2 =
                new Resource(
                        "res2",
                        "image",
                        200,
                        "/img/res2.jpg"
                );

        Resource resource3 =
                new Resource(
                        "res3",
                        "image",
                        300,
                        "/img/res3.jpg"
                );

        lfu.putInCache(resource1);
        lfu.putInCache(resource2);

        lfu.getResource("res1");

        lfu.putInCache(resource3);

        assertNull(
                lfu.getResource("res2")
        );

        assertNotNull(
                lfu.getResource("res1")
        );

        assertNotNull(
                lfu.getResource("res3")
        );
    }
}