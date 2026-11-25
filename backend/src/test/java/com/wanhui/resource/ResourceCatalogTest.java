package com.wanhui.resource;

import com.wanhui.resource.catalog.CatalogResource;
import com.wanhui.resource.catalog.ResourceCatalogLoader;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceCatalogTest {

    private final ResourceCatalogLoader loader = new ResourceCatalogLoader();

    @Test
    void loadsVersionedCatalogWithUniqueKeysAndKnownCategories() {
        var resources = loader.resources();

        assertThat(resources).hasSize(202);
        assertThat(resources).extracting(CatalogResource::key).doesNotHaveDuplicates();
        assertThat(new HashSet<>(resources.stream().map(CatalogResource::category).toList()))
                .containsExactlyInAnyOrder("academic", "design", "ai", "media");
        assertThat(resources).allSatisfy(resource -> {
            assertThat(resource.name()).isNotBlank();
            assertThat(resource.subcategory()).isNotBlank();
            assertThat(resource.tags()).isNotEmpty();
        });
    }

    @Test
    void onlyAllowsHttpAndHttpsResourceUrls() {
        Set<String> schemes = new HashSet<>();
        for (CatalogResource resource : loader.resources()) {
            URI uri = URI.create(resource.url());
            schemes.add(uri.getScheme());
            assertThat(uri.getHost()).as(resource.name()).isNotBlank();
            assertThat(uri.getScheme()).as(resource.name()).isIn("http", "https");
        }
        assertThat(schemes).containsExactlyInAnyOrder("http", "https");
    }
}
