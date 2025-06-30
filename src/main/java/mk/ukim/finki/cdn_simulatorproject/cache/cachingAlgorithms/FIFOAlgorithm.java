package mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms;

import mk.ukim.finki.cdn_simulatorproject.cache.CacheStrategy;
import mk.ukim.finki.cdn_simulatorproject.cache.CachingAlgorithmType;
import mk.ukim.finki.cdn_simulatorproject.model.ClientRequest;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;

import java.util.*;

public class FIFOAlgorithm implements CacheStrategy {
    private final int capacity;
    private final Queue<Resource> resourceQueue;
    private final Map<String, Resource> resourceMap;
    private CachingAlgorithmType cachingAlgorithmType;

    public FIFOAlgorithm(int capacity) {
        this.capacity = capacity;
        this.resourceQueue = new LinkedList<>();
        this.resourceMap = new HashMap<>();
        this.cachingAlgorithmType = CachingAlgorithmType.FIFO;
    }

    @Override
    public void removeFromCache(ClientRequest clientRequest) {
        String resourceId = clientRequest.getResourceId();
        Resource resource = resourceMap.get(resourceId);
        if (resource != null) {
            resourceQueue.remove(resource);
            resourceMap.remove(resourceId);
        }
    }

    @Override
    public void clearCache() {
        resourceQueue.clear();
        resourceMap.clear();
    }

    @Override
    public boolean isCacheFull() {
        return resourceQueue.size() >= capacity;
    }

    @Override
    public void putInCache(Resource resource) {
        String resourceId = resource.getResourceId();

        if (resourceMap.containsKey(resourceId)) {
            return;
        }

        if (isCacheFull()) {
            Resource oldest = resourceQueue.poll();
            if (oldest != null) {
                resourceMap.remove(oldest.getResourceId());
            }
        }

        resourceQueue.offer(resource);
        resourceMap.put(resourceId, resource);
    }

    @Override
    public Collection<Resource> snapshot() {
        return List.copyOf(resourceQueue);
    }

    @Override
    public Resource getResource(String resourceId) {
        return resourceMap.get(resourceId);
    }
}