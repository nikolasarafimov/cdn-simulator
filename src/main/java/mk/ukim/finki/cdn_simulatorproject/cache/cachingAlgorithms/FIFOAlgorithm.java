package mk.ukim.finki.cdn_simulatorproject.cache.cachingAlgorithms;

import mk.ukim.finki.cdn_simulatorproject.cache.CacheStrategy;
import mk.ukim.finki.cdn_simulatorproject.model.ClientRequest;
import mk.ukim.finki.cdn_simulatorproject.model.Resource;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class FIFOAlgorithm implements CacheStrategy {

    private final int capacity;
    private final Queue<Resource> resourceQueue;
    private final Map<String, Resource> resourceMap;

    public FIFOAlgorithm(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Cache capacity must be greater than zero.");
        }

        this.capacity = capacity;
        this.resourceQueue = new ArrayDeque<>();
        this.resourceMap = new HashMap<>();
    }

    @Override
    public void removeFromCache(ClientRequest clientRequest) {
        if (clientRequest == null) {
            return;
        }

        String resourceId = clientRequest.getResourceId();
        Resource resource = resourceMap.remove(resourceId);

        if (resource != null) {
            resourceQueue.remove(resource);
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
        if (resource == null || resource.getResourceId() == null) {
            return;
        }

        String resourceId = resource.getResourceId();

        if (resourceMap.containsKey(resourceId)) {
            return;
        }

        if (isCacheFull()) {
            Resource oldestResource = resourceQueue.poll();

            if (oldestResource != null) {
                resourceMap.remove(oldestResource.getResourceId());
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