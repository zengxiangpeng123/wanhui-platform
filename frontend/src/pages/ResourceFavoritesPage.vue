<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { fetchFavorites, type FavoriteResource } from '../api/userResources';
import AppShell from '../layouts/AppShell.vue';
import EmptyState from '../components/EmptyState.vue';
import ResourceCard from '../components/ResourceCard.vue';
import type { CatalogResource } from '../api/resources';

const items = ref<FavoriteResource[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

function asCard(item: FavoriteResource): CatalogResource {
  return {
    key: item.resourceKey,
    name: item.name,
    description: item.description,
    category: item.category,
    subcategory: item.subcategory,
    url: item.url,
    icon: item.icon,
    tags: []
  };
}

onMounted(async () => {
  try {
    items.value = (await fetchFavorites()).items;
  } catch (requestError) {
    error.value = requestError instanceof Error ? requestError.message : '收藏加载失败';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <AppShell>
    <section class="user-page">
      <router-link class="back-link" to="/user">返回个人中心</router-link>
      <p class="eyebrow">MY COLLECTION</p>
      <h1>资源收藏</h1>
      <div v-if="loading" class="loading">正在加载收藏…</div>
      <el-alert v-else-if="error" :title="error" type="error" :closable="false" show-icon />
      <EmptyState v-else-if="items.length === 0" title="还没有收藏资源" description="在资源首页点击收藏后，资源会出现在这里。">
        <router-link to="/">去发现资源</router-link>
      </EmptyState>
      <div v-else class="favorite-grid">
        <template v-for="item in items" :key="item.resourceKey">
          <ResourceCard v-if="item.available" :resource="asCard(item)" />
          <article v-else class="unavailable-card">
            <strong>{{ item.name }}</strong>
            <span>该资源已从目录下架</span>
          </article>
        </template>
      </div>
    </section>
  </AppShell>
</template>

<style scoped>
.user-page { max-width: 1120px; margin: 0 auto; padding: 42px clamp(18px, 4vw, 48px) 72px; }
.back-link { color: #667085; font-size: 14px; text-decoration: none; }
.eyebrow { margin: 26px 0 10px; color: #4056ed; font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0 0 26px; color: #172033; font-size: clamp(34px, 6vw, 54px); }
.loading { color: #667085; }
.favorite-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.unavailable-card { display: flex; min-height: 120px; align-items: center; justify-content: center; flex-direction: column; gap: 8px; border: 1px dashed #d0d5dd; border-radius: 14px; color: #98a2b3; background: #fbfcff; }
.unavailable-card strong { color: #667085; }
</style>
