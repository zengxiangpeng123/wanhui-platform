package com.wanhui.resource.service;

import com.wanhui.common.exception.BusinessException;
import com.wanhui.resource.catalog.CatalogResource;
import com.wanhui.resource.catalog.ResourceCatalogLoader;
import com.wanhui.resource.dto.FavoritePage;
import com.wanhui.resource.dto.FavoriteResourceResponse;
import com.wanhui.resource.dto.RecentVisitResponse;
import com.wanhui.resource.dto.UserSiteRequest;
import com.wanhui.resource.dto.UserSiteResponse;
import com.wanhui.resource.entity.RecentVisit;
import com.wanhui.resource.entity.ResourceFavorite;
import com.wanhui.resource.entity.UserSite;
import com.wanhui.resource.mapper.RecentVisitMapper;
import com.wanhui.resource.mapper.ResourceFavoriteMapper;
import com.wanhui.resource.mapper.UserSiteMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserResourceService {
    private static final int MAX_RECENT_VISITS = 20;
    private static final int MAX_PAGE_SIZE = 50;
    private final ResourceCatalogLoader catalogLoader;
    private final ResourceFavoriteMapper favoriteMapper;
    private final RecentVisitMapper recentVisitMapper;
    private final UserSiteMapper userSiteMapper;

    public UserResourceService(
            ResourceCatalogLoader catalogLoader,
            ResourceFavoriteMapper favoriteMapper,
            RecentVisitMapper recentVisitMapper,
            UserSiteMapper userSiteMapper
    ) {
        this.catalogLoader = catalogLoader;
        this.favoriteMapper = favoriteMapper;
        this.recentVisitMapper = recentVisitMapper;
        this.userSiteMapper = userSiteMapper;
    }

    public void addFavorite(Long userId, String resourceKey) {
        requireResource(resourceKey);
        favoriteMapper.insertIgnore(userId, resourceKey);
    }

    public void removeFavorite(Long userId, String resourceKey) {
        favoriteMapper.deleteOne(userId, resourceKey);
    }

    public FavoritePage favorites(Long userId, int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "分页参数非法");
        }
        long total = favoriteMapper.countByUser(userId);
        int offset = (page - 1) * size;
        List<FavoriteResourceResponse> items = favoriteMapper.selectPage(userId, offset, size).stream()
                .map(this::favoriteResponse)
                .toList();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new FavoritePage(items, page, size, total, totalPages);
    }

    public void recordVisit(Long userId, String resourceKey) {
        requireResource(resourceKey);
        recentVisitMapper.upsert(userId, resourceKey);
        List<RecentVisit> visits = recentVisitMapper.selectAllByUser(userId);
        for (int i = MAX_RECENT_VISITS; i < visits.size(); i++) {
            recentVisitMapper.deleteOne(userId, visits.get(i).getResourceKey());
        }
    }

    public List<RecentVisitResponse> recentVisits(Long userId) {
        return recentVisitMapper.selectAllByUser(userId).stream()
                .limit(MAX_RECENT_VISITS)
                .map(this::recentResponse)
                .toList();
    }

    public void clearRecentVisits(Long userId) {
        recentVisitMapper.clear(userId);
    }

    public UserSiteResponse addSite(Long userId, UserSiteRequest request) {
        String normalizedUrl = normalizeUrl(request.url());
        UserSite site = new UserSite();
        site.setUserId(userId);
        site.setName(request.name().trim());
        site.setUrl(normalizedUrl);
        site.setNormalizedUrl(normalizedUrl);
        site.setUrlHash(sha256(normalizedUrl));
        site.setIconUrl(request.iconUrl());
        site.setCreatedAt(LocalDateTime.now());
        try {
            userSiteMapper.insert(site);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "CONFLICT", "该网站已添加");
        }
        return siteResponse(site);
    }

    public List<UserSiteResponse> sites(Long userId) {
        return userSiteMapper.selectByUser(userId).stream().map(this::siteResponse).toList();
    }

    public void deleteSite(Long userId, Long siteId) {
        userSiteMapper.deleteOwned(siteId, userId);
    }

    private CatalogResource requireResource(String resourceKey) {
        CatalogResource resource = catalogLoader.findByKey(resourceKey);
        if (resource == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "资源不存在");
        }
        return resource;
    }

    private FavoriteResourceResponse favoriteResponse(ResourceFavorite favorite) {
        CatalogResource resource = catalogLoader.findByKey(favorite.getResourceKey());
        if (resource == null) {
            return new FavoriteResourceResponse(
                    favorite.getResourceKey(), "资源已下架", "", "", "", "", "", false, favorite.getCreatedAt()
            );
        }
        return new FavoriteResourceResponse(
                resource.key(), resource.name(), resource.description(), resource.category(),
                resource.subcategory(), resource.url(), resource.icon(), true, favorite.getCreatedAt()
        );
    }

    private RecentVisitResponse recentResponse(RecentVisit visit) {
        CatalogResource resource = catalogLoader.findByKey(visit.getResourceKey());
        return new RecentVisitResponse(
                visit.getResourceKey(),
                resource == null ? "资源已下架" : resource.name(),
                visit.getVisitedAt()
        );
    }

    private UserSiteResponse siteResponse(UserSite site) {
        return new UserSiteResponse(
                String.valueOf(site.getId()),
                site.getName(),
                site.getUrl(),
                site.getIconUrl(),
                site.getCreatedAt()
        );
    }

    private String normalizeUrl(String rawUrl) {
        try {
            URI uri = URI.create(rawUrl.trim());
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
            if (!("http".equals(scheme) || "https".equals(scheme)) || host.isBlank()) {
                throw new IllegalArgumentException("bad URL");
            }
            String path = uri.getPath();
            if (path == null || path.isBlank() || "/".equals(path)) {
                path = "";
            }
            String port = uri.getPort() == -1 || (("http".equals(scheme) && uri.getPort() == 80)
                    || ("https".equals(scheme) && uri.getPort() == 443))
                    ? "" : ":" + uri.getPort();
            String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
            return scheme + "://" + host + port + path + query;
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "网站地址必须是合法的 http/https URL");
        }
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
