<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  fetchCategories,
  fetchPosts,
  searchPosts,
  type PostCategory,
  type PostDetail,
  type PostPage
} from '../api/forum';
import AppShell from '../layouts/AppShell.vue';
import PostCard from '../components/PostCard.vue';
import PostEditorDialog from '../components/PostEditorDialog.vue';
import { useAuthStore } from '../stores/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const categories = ref<PostCategory[]>([]);
const pageData = ref<PostPage>({ items: [], page: 1, size: 20, total: 0, totalPages: 0 });
const loading = ref(false);
const error = ref<string | null>(null);
const editorOpen = ref(false);
const searchText = ref('');

const activeCategory = computed(() => {
  const value = route.query.category;
  return typeof value === 'string' ? value : '';
});
const activeKeyword = computed(() => {
  const value = route.query.q;
  return typeof value === 'string' ? value : '';
});
const currentPage = computed(() => {
  const value = Number(route.query.page);
  return Number.isInteger(value) && value > 0 ? value : 1;
});

async function load() {
  loading.value = true;
  error.value = null;
  try {
    pageData.value = activeKeyword.value
      ? await searchPosts(activeKeyword.value, currentPage.value, 20)
      : await fetchPosts(activeCategory.value || undefined, currentPage.value, 20);
  } catch (requestError) {
    error.value = requestError instanceof Error ? requestError.message : '帖子加载失败';
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

function selectCategory(code: string) {
  updateQuery({ category: code || undefined, q: undefined });
}

function applySearch() {
  const keyword = searchText.value.trim();
  updateQuery({ q: keyword || undefined, category: undefined });
}

function clearSearch() {
  searchText.value = '';
  updateQuery({ q: undefined });
}

function changePage(page: number) {
  if (page < 1 || page > pageData.value.totalPages) return;
  updateQuery({ page: String(page) });
}

function openEditor() {
  if (!auth.isAuthenticated) {
    router.push({ path: '/login', query: { redirect: route.fullPath } });
    return;
  }
  editorOpen.value = true;
}

function insertCreatedPost(post: PostDetail) {
  const summary = {
    ...post,
    excerpt: post.content.replace(/\s+/g, ' ').slice(0, 180)
  };
  pageData.value = {
    ...pageData.value,
    items: [summary, ...pageData.value.items],
    total: pageData.value.total + 1
  };
}

watch(() => [route.query.category, route.query.page, route.query.q], load, { immediate: true });

onMounted(async () => {
  searchText.value = activeKeyword.value;
  if (!auth.initialized) await auth.loadMe();
  try {
    categories.value = await fetchCategories();
  } catch {
    categories.value = [];
  }
});
</script>

<template>
  <AppShell>
    <template #sidebar>
      <div class="forum-sidebar">
        <nav class="category-nav" aria-label="帖子分类">
          <p class="nav-title">论坛分类</p>
          <button
            type="button"
            class="category-item"
            :class="{ active: !activeCategory && !activeKeyword }"
            data-testid="category-all"
            @click="selectCategory('')"
          >全部帖子</button>
          <button
            v-for="category in categories"
            :key="category.code"
            type="button"
            class="category-item"
            :class="{ active: activeCategory === category.code }"
            :data-testid="`category-${category.code}`"
            @click="selectCategory(category.code)"
          >{{ category.name }}</button>
        </nav>
        <section class="community-tip">
          <h2>社区提示</h2>
          <p>发帖前请选择合适分类，避免重复发布相同主题。尊重原创内容，违规广告和引战内容会被处理。</p>
        </section>
      </div>
    </template>

    <section class="forum-page">
      <header class="forum-hero">
        <div class="hero-inner">
          <h1>万汇网社区论坛</h1>
          <p>与同好分享资源、经验和问题，让好内容被更多人看见。</p>
          <button type="button" class="create-button" data-testid="open-editor" @click="openEditor">＋ 发布新主题</button>
        </div>
      </header>

      <form class="search-form" role="search" @submit.prevent="applySearch">
        <input
          v-model="searchText"
          data-testid="forum-search"
          type="search"
          placeholder="搜索帖子标题、正文或标签"
        />
        <button type="submit" data-testid="forum-search-submit">搜索</button>
        <button
          v-if="activeKeyword"
          type="button"
          class="clear-button"
          data-testid="forum-search-clear"
          @click="clearSearch"
        >清空</button>
      </form>

      <div class="result-heading">
        <h2 v-if="activeKeyword">搜索“{{ activeKeyword }}”</h2>
        <h2 v-else>{{ activeCategory ? categories.find((item) => item.code === activeCategory)?.name || '帖子' : '全部帖子' }}</h2>
        <span class="result-count">共 {{ pageData.total }} 篇</span>
      </div>

      <div v-if="loading" class="state-panel">正在加载帖子…</div>
      <div v-else-if="error" class="state-panel error-panel">
        <strong>{{ error }}</strong>
        <button type="button" data-testid="forum-retry" @click="load">重试</button>
      </div>
      <div v-else-if="pageData.items.length === 0 && activeKeyword" class="state-panel" data-testid="forum-empty">
        <strong>没有找到相关帖子</strong>
        <span>换个关键词试试，或清空搜索条件。</span>
        <button type="button" @click="clearSearch">清空搜索</button>
      </div>
      <div v-else-if="pageData.items.length === 0" class="state-panel">
        <strong>还没有帖子</strong>
        <span>成为第一个分享的人吧。</span>
        <button type="button" @click="openEditor">发布新帖</button>
      </div>
      <div v-else class="post-list">
        <PostCard v-for="post in pageData.items" :key="post.id" :post="post" />
      </div>

      <div v-if="!loading && pageData.totalPages > 1" class="pagination">
        <button type="button" :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
        <span>第 {{ currentPage }} / {{ pageData.totalPages }} 页</span>
        <button type="button" :disabled="currentPage >= pageData.totalPages" @click="changePage(currentPage + 1)">下一页</button>
      </div>

      <PostEditorDialog
        v-model="editorOpen"
        :categories="categories"
        :default-category="activeCategory || undefined"
        @created="insertCreatedPost"
        @unauthorized="router.push({ path: '/login', query: { redirect: route.fullPath } })"
      />
    </section>
  </AppShell>
</template>

<style scoped>
.forum-sidebar { display: grid; gap: 15px; }
.category-nav, .community-tip { padding: 18px; border: 1px solid #dfe6e1; border-radius: var(--radius); background: #fff; box-shadow: var(--shadow-sm); }
.nav-title { margin: 0 0 11px; color: var(--ink); font-size: 14px; font-weight: 800; }
.category-item { display: flex; align-items: center; gap: 10px; width: 100%; min-height: 40px; padding: 8px 10px; border: 0; border-radius: 11px; color: #4b5651; background: transparent; text-align: left; }
.category-item::before { content: ""; width: 7px; height: 7px; border-radius: 50%; background: #c9d2cd; }
.category-item:hover { color: var(--brand); background: #f0f5f1; }
.category-item.active { color: var(--brand); background: #e7f1ed; box-shadow: inset 3px 0 0 var(--brand); font-weight: 700; }
.category-item.active::before { background: var(--brand); }
.community-tip h2 { margin: 0 0 8px; font-size: 14px; }
.community-tip p { margin: 0; color: var(--muted); font-size: 13px; line-height: 1.7; }
.forum-page { min-width: 0; }
.forum-hero { position: relative; overflow: hidden; padding: 34px 28px; border-radius: 18px; color: #fff; background: #173f38; box-shadow: 0 16px 34px rgba(23, 63, 56, .15); text-align: center; }
.forum-hero::before, .forum-hero::after { content: ""; position: absolute; border-radius: 50%; background: rgba(255, 255, 255, .055); }
.forum-hero::before { top: -80px; right: -45px; width: 210px; height: 210px; }
.forum-hero::after { right: 110px; bottom: -55px; width: 120px; height: 120px; }
.hero-inner { position: relative; z-index: 1; max-width: 700px; margin: 0 auto; }
h1 { margin: 0; color: #fff; font-size: clamp(28px, 4vw, 34px); letter-spacing: -.02em; }
.hero-inner p { margin: 9px 0 20px; color: rgba(255, 255, 255, .88); font-size: 14px; }
.create-button { min-height: 44px; padding: 0 18px; border: 0; border-radius: 999px; color: var(--brand); background: #fff; box-shadow: 0 14px 26px rgba(21, 23, 63, .18); font-weight: 700; }
.search-form { display: flex; gap: 10px; margin: 20px 0 0; padding: 10px; border: 1px solid #dfe6e1; border-radius: 14px; background: #fff; box-shadow: var(--shadow-sm); }
.search-form input { flex: 1; min-width: 0; padding: 10px 12px; border: 1px solid #d5ded8; border-radius: 9px; outline: none; }
.search-form input:focus { border-color: #75a99a; box-shadow: 0 0 0 4px rgba(23, 107, 91, .09); }
.search-form button { padding: 9px 16px; border: 0; border-radius: 9px; color: #fff; background: var(--brand); font-weight: 650; }
.search-form button:hover { background: var(--brand-dark); }
.search-form .clear-button { border: 1px solid #d5ded8; color: #55615c; background: #fff; }
.result-heading { display: flex; align-items: baseline; justify-content: space-between; gap: 16px; padding: 22px 0 14px; }
h2 { margin: 0; color: var(--ink); font-size: 19px; }
.result-count { color: #8a958f; font-size: 13px; }
.post-list { display: grid; gap: 13px; }
.state-panel { display: flex; align-items: center; justify-content: center; min-height: 220px; flex-direction: column; gap: 10px; padding: 24px; border: 1px dashed #cbd6d0; border-radius: 16px; color: var(--muted); background: rgba(255, 255, 255, .72); text-align: center; }
.state-panel strong { color: var(--ink); font-size: 18px; }
.state-panel button, .pagination button { padding: 9px 14px; border: 0; border-radius: 9px; color: #fff; background: var(--brand); font-weight: 650; }
.error-panel { border-color: #f0c7c7; background: #fffafa; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 16px; margin-top: 26px; color: var(--muted); font-size: 13px; }
.pagination button:disabled { cursor: not-allowed; opacity: .45; }
@media (max-width: 820px) {
  .forum-sidebar { display: block; }
  .community-tip { display: none; }
  .category-nav { padding: 12px 14px; }
  .nav-title { display: none; }
  .category-item { display: inline-flex; width: auto; min-height: 36px; padding: 7px 12px; margin-right: 5px; border-radius: 999px; white-space: nowrap; }
  .category-item.active { box-shadow: none; }
  .category-nav { overflow-x: auto; white-space: nowrap; scrollbar-width: none; }
  .category-nav::-webkit-scrollbar { display: none; }
}
@media (max-width: 640px) {
  .forum-hero { padding: 28px 18px; }
  .hero-inner h1 { font-size: 27px; }
  .search-form { flex-wrap: wrap; }
  .search-form input { flex-basis: 100%; }
  .result-heading { align-items: flex-start; flex-direction: column; gap: 4px; }
}
</style>