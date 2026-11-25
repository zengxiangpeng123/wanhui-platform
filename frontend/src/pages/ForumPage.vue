<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchCategories, fetchPosts, type PostCategory, type PostDetail, type PostPage } from '../api/forum';
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

const activeCategory = computed(() => {
  const value = route.query.category;
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
    pageData.value = await fetchPosts(activeCategory.value || undefined, currentPage.value, 20);
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
  updateQuery({ category: code || undefined });
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

watch(() => [route.query.category, route.query.page], load, { immediate: true });

onMounted(async () => {
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
      <nav class="category-nav" aria-label="帖子分类">
        <p class="nav-title">分类</p>
        <button
          type="button"
          class="category-item"
          :class="{ active: !activeCategory }"
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
    </template>

    <section class="forum-page">
      <header class="forum-hero">
        <div>
          <p class="eyebrow">COMMUNITY</p>
          <h1>万汇论坛</h1>
          <p class="lead">分享资源使用经验，交流工具心得，提出你的问题。</p>
        </div>
        <button type="button" class="create-button" data-testid="open-editor" @click="openEditor">发布新帖</button>
      </header>

      <div class="result-heading">
        <h2>{{ activeCategory ? categories.find((item) => item.code === activeCategory)?.name || '帖子' : '全部帖子' }}</h2>
        <span class="result-count">共 {{ pageData.total }} 篇</span>
      </div>

      <div v-if="loading" class="state-panel">正在加载帖子…</div>
      <div v-else-if="error" class="state-panel error-panel">
        <strong>{{ error }}</strong>
        <button type="button" data-testid="forum-retry" @click="load">重试</button>
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
.category-nav { display: flex; flex-direction: column; gap: 4px; padding: 28px 16px; }
.nav-title { margin: 0 0 8px; padding-left: 10px; color: #98a2b3; font-size: 12px; font-weight: 700; letter-spacing: .12em; }
.category-item { padding: 10px; border: 0; border-radius: 9px; color: #667085; background: transparent; font: inherit; text-align: left; cursor: pointer; }
.category-item:hover { background: #f4f6ff; color: #4056ed; }
.category-item.active { color: #4056ed; background: #f1f3ff; font-weight: 700; }
.forum-page { max-width: 980px; margin: 0 auto; padding: 34px clamp(18px, 4vw, 48px) 72px; }
.forum-hero { display: flex; align-items: flex-end; justify-content: space-between; gap: 20px; padding: 24px 0 28px; border-bottom: 1px solid #edf0f8; }
.eyebrow { margin: 0 0 8px; color: #4056ed; font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0; color: #172033; font-size: clamp(30px, 5vw, 46px); letter-spacing: -.04em; }
.lead { margin: 12px 0 0; color: #667085; }
.create-button, .state-panel button, .pagination button { padding: 11px 18px; border: 0; border-radius: 9px; color: #fff; background: #4056ed; font: inherit; font-weight: 700; cursor: pointer; }
.result-heading { display: flex; align-items: baseline; justify-content: space-between; gap: 16px; padding: 24px 0 16px; }
h2 { margin: 0; color: #172033; font-size: 20px; }
.result-count { color: #98a2b3; font-size: 13px; }
.post-list { display: flex; flex-direction: column; gap: 14px; }
.state-panel { display: flex; align-items: center; justify-content: center; min-height: 200px; flex-direction: column; gap: 10px; border: 1px dashed #d0d5dd; border-radius: 14px; color: #667085; background: #fbfcff; text-align: center; }
.state-panel strong { color: #344054; font-size: 18px; }
.error-panel { border-color: #fecdca; background: #fffbfa; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 16px; margin-top: 28px; color: #667085; }
.pagination button:disabled { cursor: not-allowed; opacity: .45; }
@media (max-width: 640px) { .forum-hero { align-items: flex-start; flex-direction: column; } }
</style>
