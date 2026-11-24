<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchResources, type CatalogPage } from '../api/resources';
import AppShell from '../layouts/AppShell.vue';
import AppSidebar, { type ResourceCategory } from '../components/AppSidebar.vue';
import ResourceCard from '../components/ResourceCard.vue';

const categories: ResourceCategory[] = [
  { value: '', label: '全部资源' },
  { value: 'design', label: '设计资源' },
  { value: 'ai', label: 'AI 工具' },
  { value: 'academic', label: '学术资源' },
  { value: 'media', label: '媒体资源' }
];

const route = useRoute();
const router = useRouter();
const searchText = ref('');
const loading = ref(false);
const error = ref<string | null>(null);
const pageData = ref<CatalogPage>({
  items: [],
  page: 1,
  size: 24,
  total: 0,
  totalPages: 0,
  sort: 'catalogOrder,asc'
});

const activeCategory = computed(() => queryString('category'));
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
  searchText.value = queryString('q');
  try {
    pageData.value = await fetchResources({
      category: activeCategory.value || undefined,
      q: queryString('q') || undefined,
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

function applySearch() {
  updateQuery({ q: searchText.value.trim() || undefined });
}

function clearSearch() {
  searchText.value = '';
  updateQuery({ q: undefined });
}

function changePage(page: number) {
  if (page < 1 || page > pageData.value.totalPages) return;
  updateQuery({ page: String(page) });
}

watch(() => [route.query.category, route.query.q, route.query.page], load, { immediate: true });

onMounted(() => {
  if (!route.query.q) searchText.value = '';
});
</script>

<template>
  <AppShell>
    <template #sidebar>
      <AppSidebar :active-category="activeCategory" :categories="categories" @select="selectCategory" />
    </template>

    <section class="resource-home">
      <div class="resource-hero">
        <p class="eyebrow">RESOURCE LIBRARY</p>
        <h1>发现好资源</h1>
        <p class="lead">按分类浏览，或搜索工具、资料和灵感。</p>
        <form class="search-form" @submit.prevent="applySearch">
          <input v-model="searchText" data-testid="resource-search" type="search" placeholder="搜索资源名称、简介或标签" />
          <button type="submit" data-testid="resource-search-submit">搜索</button>
        </form>
      </div>

      <div class="result-heading">
        <h2>{{ activeCategory ? categories.find((item) => item.value === activeCategory)?.label : '全部资源' }}</h2>
        <span class="result-count">共 {{ pageData.total }} 个资源</span>
      </div>

      <div v-if="loading" class="state-panel">正在加载资源…</div>
      <el-alert v-else-if="error" :title="error" type="error" :closable="false" show-icon />
      <div v-else-if="pageData.items.length === 0" class="state-panel empty-state">
        <strong>没有找到匹配资源</strong>
        <span>试试其他关键词，或清空搜索条件。</span>
        <button type="button" data-testid="clear-search" @click="clearSearch">清空搜索</button>
      </div>
      <div v-else class="resource-grid">
        <ResourceCard v-for="resource in pageData.items" :key="resource.key" :resource="resource" />
      </div>

      <div v-if="!loading && pageData.totalPages > 1" class="pagination">
        <button type="button" :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
        <span>第 {{ currentPage }} / {{ pageData.totalPages }} 页</span>
        <button type="button" :disabled="currentPage >= pageData.totalPages" @click="changePage(currentPage + 1)">下一页</button>
      </div>
    </section>
  </AppShell>
</template>

<style scoped>
.resource-home { max-width: 1240px; margin: 0 auto; padding: 42px clamp(18px, 4vw, 48px) 72px; }
.resource-hero { padding: 38px 0 30px; border-bottom: 1px solid #edf0f8; }
.eyebrow { margin: 0 0 10px; color: #4056ed; font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0; color: #172033; font-size: clamp(38px, 6vw, 62px); letter-spacing: -.04em; }
.lead { margin: 14px 0 24px; color: #667085; font-size: 17px; }
.search-form { display: flex; max-width: 620px; gap: 10px; }
.search-form input { flex: 1; min-width: 0; padding: 12px 14px; border: 1px solid #d0d5dd; border-radius: 9px; font: inherit; }
.search-form button, .state-panel button, .pagination button { padding: 11px 16px; border: 0; border-radius: 9px; color: #fff; background: #4056ed; font: inherit; font-weight: 700; cursor: pointer; }
.result-heading { display: flex; align-items: baseline; justify-content: space-between; gap: 16px; padding: 28px 0 18px; }
h2 { margin: 0; color: #172033; font-size: 22px; }
.result-count { color: #98a2b3; font-size: 14px; }
.resource-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.state-panel { display: flex; align-items: center; justify-content: center; min-height: 180px; flex-direction: column; gap: 10px; color: #667085; border: 1px dashed #d0d5dd; border-radius: 14px; background: #fbfcff; }
.empty-state strong { color: #344054; font-size: 18px; }
.state-panel button { margin-top: 8px; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 16px; margin-top: 28px; color: #667085; }
.pagination button:disabled { cursor: not-allowed; opacity: .45; }
@media (max-width: 560px) { .search-form { flex-direction: column; } .result-heading { align-items: flex-start; flex-direction: column; gap: 6px; } }
</style>
