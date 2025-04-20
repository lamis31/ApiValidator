package com.saltsecurity.assignment.service;

import com.saltsecurity.assignment.model.ApiModel;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ModelStorageService {

    // Partition by HTTP method first for faster lookups
    private final Map<String, Map<String, ApiModel>> methodToPathMap = new ConcurrentHashMap<>();
    
    // Cache for frequently accessed models
    private final Map<String, ApiModel> modelCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 100;
    
    /**
     * Stores a list of API models. If a model with the same key (method + path)
     * already exists, it will be overwritten.
     *
     * @param models The list of ApiModel objects to store.
     */
    public void storeModels(List<ApiModel> models) {
        if (models == null) {
            return;
        }
        
        models.forEach(model -> {
            if (isModelValid(model)) {
                storeModel(model);
            } else {
                log.warn("Invalid model: {}", model);
            }
        });
    }
    
    /**
     * Stores a single API model.
     * 
     * @param model The ApiModel to store
     */
    private void storeModel(ApiModel model) {
        String method = model.getMethod().toUpperCase();
        String normalizedPath = normalizePath(model.getPath());
        
        // Get or create the path map for this method
        Map<String, ApiModel> pathMap = methodToPathMap.computeIfAbsent(method, k -> new ConcurrentHashMap<>());
        
        // Store the model
        pathMap.put(normalizedPath, model);
        
        // Add to cache with composite key
        String key = method + ":" + normalizedPath;
        modelCache.put(key, model);
        
        log.info("Stored model with key {}", key);
        
        // Prune cache if it grows too large
        if (modelCache.size() > MAX_CACHE_SIZE) {
            // Simple pruning - just clear half the cache
            // In a production system, you would use LRU eviction or another strategy
            List<String> keys = new ArrayList<>(modelCache.keySet());
            for (int i = 0; i < keys.size() / 2; i++) {
                modelCache.remove(keys.get(i));
            }
        }
    }

    private boolean isModelValid(ApiModel model) {
        return model != null && model.getMethod() != null && model.getPath() != null;
    }

    /**
     * Finds an API model based on the HTTP method and path.
     *
     * @param method The HTTP method (e.g., "GET", "POST").
     * @param path   The API path (e.g., "/entity/create").
     * @return An Optional containing the ApiModel if found, otherwise Optional.empty().
     */
    public Optional<ApiModel> findModel(String method, String path) {
        if (method == null || path == null) {
            return Optional.empty();
        }
        
        String normalizedMethod = method.toUpperCase();
        String normalizedPath = normalizePath(path);
        
        // Try cache first for performance
        String cacheKey = normalizedMethod + ":" + normalizedPath;
        ApiModel cachedModel = modelCache.get(cacheKey);
        if (cachedModel != null) {
            log.debug("Cache hit for key: {}", cacheKey);
            return Optional.of(cachedModel);
        }
        
        // Get the path map for this method
        Map<String, ApiModel> pathMap = methodToPathMap.get(normalizedMethod);
        if (pathMap == null) {
            log.info("No models found for method: {}", normalizedMethod);
            return Optional.empty();
        }
        
        // Look for exact path match
        ApiModel model = pathMap.get(normalizedPath);
        
        if (model != null) {
            // Add to cache for future lookups
            modelCache.put(cacheKey, model);
            log.info("Found model for key: {}", cacheKey);
            return Optional.of(model);
        }
        
        log.info("No model found for method: {} and path: {}", normalizedMethod, normalizedPath);
        return Optional.empty();
    }

    /**
     * Normalizes a path to handle common variations like trailing slashes and case sensitivity.
     *
     * @param path The path to normalize.
     * @return The normalized path.
     */
    private String normalizePath(String path) {
        if (path == null) {
            return "";
        }
        
        // Convert to lowercase for case-insensitive matching
        String normalizedPath = path.toLowerCase();
        
        // Ensure path starts with a slash
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        
        // Remove trailing slash if not the root path
        if (normalizedPath.length() > 1 && normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }
        
        // Log any normalization that changed the original path
        if (!normalizedPath.equals(path)) {
            log.debug("Path normalized from '{}' to '{}'", path, normalizedPath);
        }
        
        return normalizedPath;
    }

    public void clearStore() {
        methodToPathMap.clear();
        modelCache.clear();
        log.info("Model store cleared");
    }

    public int getStoreSize() {
        int size = 0;
        for (Map<String, ApiModel> pathMap : methodToPathMap.values()) {
            size += pathMap.size();
        }
        return size;
    }
}
