package mk.ukim.finki.cdn_simulatorproject;

import mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms.LRUAlgorithm;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class LRUAlgorithmTest {

    @Test
    void shouldEvictLeastRecentlyUsedResource() {
        LRUAlgorithm lru = new LRUAlgorithm(2);

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
                        "video",
                        300,
                        "/video/res3.mp4"
                );

        lru.putInCache(resource1);
        lru.putInCache(resource2);
        lru.putInCache(resource3);

        assertNull(
                lru.getResource("res1")
        );

        assertNotNull(
                lru.getResource("res2")
        );

        assertNotNull(
                lru.getResource("res3")
        );
    }

    @Test
    void shouldRefreshAccessOrderOnCacheHit() {
        LRUAlgorithm lru = new LRUAlgorithm(2);

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
                        "video",
                        300,
                        "/video/res3.mp4"
                );

        lru.putInCache(resource1);
        lru.putInCache(resource2);

        assertNotNull(
                lru.getResource("res1")
        );

        lru.putInCache(resource3);

        assertNotNull(
                lru.getResource("res1")
        );

        assertNull(
                lru.getResource("res2")
        );

        assertNotNull(
                lru.getResource("res3")
        );
    }
}