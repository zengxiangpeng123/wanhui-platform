package com.wanhui.resource.catalog;

import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ResourceCatalogLoader {
    private static final int EXPECTED_SIZE = 202;
    private static final Set<String> CATEGORIES = Set.of("academic", "design", "ai", "media");
    private final List<CatalogResource> resources;

    public ResourceCatalogLoader() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("catalog/resources.json")) {
            if (input == null) {
                throw new IllegalStateException("catalog/resources.json is missing");
            }
            CatalogDocument document = new ObjectMapper().readValue(input, CatalogDocument.class);
            validate(document);
            this.resources = List.copyOf(document.resources());
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load resource catalog", exception);
        }
    }

    public List<CatalogResource> resources() {
        return resources;
    }

    public CatalogResource findByKey(String key) {
        return resources.stream()
                .filter(resource -> resource.key().equals(key))
                .findFirst()
                .orElse(null);
    }

    private void validate(CatalogDocument document) {
        if (document.resources() == null || document.resources().size() != EXPECTED_SIZE) {
            throw new IllegalStateException("resource catalog must contain exactly " + EXPECTED_SIZE + " entries");
        }
        Set<String> keys = new HashSet<>();
        for (CatalogResource resource : document.resources()) {
            if (resource.key() == null || resource.key().isBlank() || !keys.add(resource.key())) {
                throw new IllegalStateException("resource keys must be non-blank and unique: " + resource.key());
            }
            if (resource.name() == null || resource.name().isBlank()) {
                throw new IllegalStateException("resource name is missing: " + resource.key());
            }
            if (!CATEGORIES.contains(resource.category())) {
                throw new IllegalStateException("unknown resource category: " + resource.category());
            }
            URI uri;
            try {
                uri = URI.create(resource.url());
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException("invalid resource URL: " + resource.key(), exception);
            }
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalStateException("resource URL must be HTTP/HTTPS: " + resource.key());
            }
        }
    }

    private record CatalogDocument(String version, List<CatalogResource> resources) {
    }
}
