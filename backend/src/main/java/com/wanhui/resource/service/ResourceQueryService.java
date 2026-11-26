package com.wanhui.resource.service;

import com.wanhui.common.exception.BusinessException;
import com.wanhui.resource.catalog.CatalogResource;
import com.wanhui.resource.catalog.ResourceCatalogLoader;
import com.wanhui.resource.mapper.ResourceFavoriteMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ResourceQueryService {
    private static final int MAX_PAGE_SIZE = 24;
    private final ResourceCatalogLoader catalogLoader;
    private final ResourceFavoriteMapper favoriteMapper;

    public ResourceQueryService(ResourceCatalogLoader catalogLoader, ResourceFavoriteMapper favoriteMapper) {
        this.catalogLoader = catalogLoader;
        this.favoriteMapper = favoriteMapper;
    }

    public CatalogPage query(String category, String keyword, int page, int size, Long viewerId) {
        if (page < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "page 必须从 1 开始");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "size 必须在 1～24 之间");
        }

        String categoryFilter = normalize(category);
        String keywordFilter = normalize(keyword);
        List<CatalogResource> filtered = catalogLoader.resources().stream()
                .filter(resource -> categoryFilter.isEmpty()
                        || resource.category().equalsIgnoreCase(categoryFilter))
                .filter(resource -> matches(resource, keywordFilter))
                .toList();

        long total = filtered.size();
        int from = Math.min((page - 1) * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<CatalogResource> items = from >= filtered.size() ? List.of() : filtered.subList(from, to);
        Set<String> favorited = favoritedKeys(viewerId);
        items = markFavorited(items, favorited);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new CatalogPage(items, page, size, total, totalPages, "catalogOrder,asc");
    }

    public CatalogResource findByKey(String resourceKey, Long viewerId) {
        CatalogResource resource = catalogLoader.findByKey(resourceKey);
        if (resource == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "资源不存在");
        }
        return markFavorited(resource, favoritedKeys(viewerId).contains(resource.key()));
    }

    private Set<String> favoritedKeys(Long viewerId) {
        if (viewerId == null) {
            return Set.of();
        }
        return new HashSet<>(favoriteMapper.selectKeysByUser(viewerId));
    }

    private CatalogResource markFavorited(CatalogResource resource, boolean favorited) {
        return new CatalogResource(
                resource.key(),
                resource.name(),
                resource.description(),
                resource.category(),
                resource.subcategory(),
                resource.url(),
                resource.icon(),
                resource.tags(),
                favorited
        );
    }

    private List<CatalogResource> markFavorited(List<CatalogResource> resources, Set<String> favorited) {
        return resources.stream()
                .map(resource -> markFavorited(resource, favorited.contains(resource.key())))
                .toList();
    }

    private boolean matches(CatalogResource resource, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        return contains(resource.name(), keyword)
                || contains(resource.description(), keyword)
                || contains(resource.subcategory(), keyword)
                || resource.tags().stream().anyMatch(tag -> contains(tag, keyword));
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
