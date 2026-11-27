<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchResources, favoriteResource, unfavoriteResource, type CatalogPage, type CatalogResource } from '../api/resources';
import AppShell from '../layouts/AppShell.vue';
import AppSidebar, { type ResourceCategory } from '../components/AppSidebar.vue';
import ResourceCard from '../components/ResourceCard.vue';
import { useAuthStore } from '../stores/auth';

const categories: ResourceCategory[] = [
  { value: '', label: '全部精选' },
  { value: 'design', label: '设计工具' },
  { value: 'ai', label: 'AI 工具' },
  { value: 'academic', label: '学术资源' },
  { value: 'media', label: '影视资源' }
];

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const loading = ref(false);
const error = ref<string | null>(null);
const favoritingKey = ref<string | null>(null);
const pageData = ref<CatalogPage>({
  items: [],
  page: 1,
  size: 24,
  total: 0,
  totalPages: 0,
  sort: 'catalogOrder,asc'
});

const activeCategory = computed(() => queryString('category'));
const activeKeyword = computed(() => queryString('q'));
const currentPage = computed(() => positiveNumber(queryString('page'), 1));

function queryString(key: string) {
  const value = route.query[key];
  return typeof value === 'string' ? value : '';
}

function positiveNumber(value: string, fallback: number) {
  const number = Number(value);
  return Number.isInteger(number) && number > 0 ? number : fallback;
}

async function load() {
  loading.value = true;
  error.value = null;
  try {
    pageData.value = await fetchResources({
      category: activeCategory.value || undefined,
      q: activeKeyword.value || undefined,
      page: currentPage.value,
      size: 24
    });
  } catch (requestError) {
    error.value = requestError instanceof Error ? requestError.message : '资源加载失败';
    pageData.value = { ...pageData.value, items: [], total: 0, totalPages: 0 };
  } finally {
    loading.value = false;
  }
}

function updateQuery(patch: Record<string, string | undefined>) {
  const query: Record<string, string> = {};
  for (const [key, value] of Object.entries({ ...route.query, ...patch })) {
    if (typeof value === 'string' && value) query[key] = value;
  }
  if (!('page' in patch)) delete query.page;
  router.push({ query });
}

function selectCategory(category: string) {
  updateQuery({ category: category || undefined });
}

function clearSearch() {
  updateQuery({ q: undefined });
}

function changePage(page: number) {
  if (page < 1 || page > pageData.value.totalPages) return;
  updateQuery({ page: String(page) });
}

async function toggleFavorite(resource: CatalogResource) {
  if (!auth.isAuthenticated) {
    router.push({ path: '/login', query: { redirect: route.fullPath } });
    return;
  }
  favoritingKey.value = resource.key;
  try {
    if (resource.favorited) {
      await unfavoriteResource(resource.key);
    } else {
      await favoriteResource(resource.key);
    }
    resource.favorited = !resource.favorited;
  } catch (requestError) {
    error.value = requestError instanceof Error ? requestError.message : '收藏操作失败';
  } finally {
    favoritingKey.value = null;
  }
}

watch(() => [route.query.category, route.query.q, route.query.page], load, { immediate: true });
</script>

<template>
  <AppShell>
    <template #sidebar>
      <AppSidebar :active-category="activeCategory" :categories="categories" @select="selectCategory" />
    </template>

    <section class="resource-home">
      <header class="page-head">
        <div>
          <span class="eyebrow">✦ 202 个精选资源 · 每周更新</span>
          <h1>{{ activeKeyword ? `搜索“${activeKeyword}”` : '发现值得收藏的好工具' }}</h1>
          <p>按领域精选网站与工具，登录后即可收藏，并在不同设备继续使用。</p>
        </div>
        <router-link class="custom-site-link" to="/user/sites">自定义网站</router-link>
      </header>

      <div class="result-toolbar">
        <span>展示 {{ pageData.items.length }} / {{ pageData.total }} 个匹配资源</span>
        <span>只读静态目录 · 官方维护</span>
      </div>

      <div v-if="loading" class="state-panel">正在加载资源…</div>
      <el-alert v-else-if="error" :title="error" type="error" :closable="false" show-icon />
      <div v-else-if="pageData.items.length === 0" class="state-panel empty-state">
        <strong>{{ activeKeyword ? `未找到与“${activeKeyword}”相关的资源` : '当前分类暂无资源' }}</strong>
        <span>试试其他关键词，或返回全部资源。</span>
        <button type="button" data-testid="clear-search" @click="clearSearch">查看全部资源</button>
      </div>
      <div v-else class="resource-grid">
        <ResourceCard
          v-for="resource in pageData.items"
          :key="resource.key"
          :resource="resource"
          :favoriting="favoritingKey === resource.key"
          @toggle-favorite="toggleFavorite"
        />
      </div>

      <div v-if="!loading && pageData.totalPages > 1" class="pagination">
        <button type="button" :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
        <span>第 {{ currentPage }} / {{ pageData.totalPages }} 页 · 共 {{ pageData.total }} 个资源</span>
        <button type="button" :disabled="currentPage >= pageData.totalPages" @click="changePage(currentPage + 1)">下一页</button>
      </div>
    </section>
  </AppShell>
</template>

<style scoped>
.resource-home { min-width: 0; }
.page-head { display: flex; align-items: flex-end; justify-content: space-between; gap: 18px; margin-bottom: 16px; }
.eyebrow { display: inline-flex; margin-bottom: 10px; padding: 5px 10px; border-radius: 999px; color: var(--brand); background: #e7f1ed; font-size: 12px; font-weight: 700; }
h1 { margin: 0; color: var(--ink); font-size: clamp(26px, 3vw, 31px); line-height: 1.25; letter-spacing: -.025em; }
.page-head p { margin: 9px 0 0; color: var(--muted); font-size: 14px; }
.custom-site-link { display: inline-flex; flex: 0 0 auto; align-items: center; min-height: 40px; padding: 0 14px; border: 1px solid #d9e2dc; border-radius: 10px; color: var(--brand); background: #fff; box-shadow: var(--shadow-sm); font-size: 14px; font-weight: 650; text-decoration: none; }
.result-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin: 18px 0 15px; color: #87928c; font-size: 13px; }
.resource-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.state-panel { display: flex; align-items: center; justify-content: center; min-height: 220px; flex-direction: column; gap: 10px; padding: 24px; border: 1px dashed #cbd6d0; border-radius: 16px; color: var(--muted); background: rgba(255, 255, 255, .72); text-align: center; }
.empty-state strong { color: var(--ink); font-size: 18px; }
.state-panel button, .pagination button { padding: 9px 14px; border: 0; border-radius: 9px; color: #fff; background: var(--brand); font-weight: 650; }
.state-panel button:hover, .pagination button:hover { background: var(--brand-dark); }
.pagination { display: flex; align-items: center; justify-content: center; gap: 16px; margin-top: 28px; color: var(--muted); font-size: 13px; }
.pagination button:disabled { cursor: not-allowed; opacity: .45; }
@media (max-width: 1180px) { .resource-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 760px) {
  .page-head { align-items: flex-start; flex-direction: column; }
  .custom-site-link { width: 100%; justify-content: center; }
  .result-toolbar { align-items: flex-start; flex-direction: column; gap: 4px; }
  .resource-grid { grid-template-columns: 1fr; }
  .pagination { flex-wrap: wrap; }
}
</style>