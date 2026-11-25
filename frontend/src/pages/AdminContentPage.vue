<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { deleteAdminPost, fetchAdminPosts, type AdminPostPage } from '../api/admin';
import AppShell from '../layouts/AppShell.vue';
import { useAuthStore } from '../stores/auth';

const router = useRouter();
const auth = useAuthStore();

const pageData = ref<AdminPostPage>({ items: [], page: 1, size: 20, total: 0, totalPages: 0 });
const loading = ref(true);
const error = ref<string | null>(null);
const page = ref(1);
const deleting = ref<string | null>(null);

const isAdmin = computed(
  () => auth.user?.roles.includes('ADMIN') || auth.user?.permissions.includes('ADMIN_CONTENT_VIEW')
);

async function load(targetPage = page.value) {
  loading.value = true;
  error.value = null;
  try {
    pageData.value = await fetchAdminPosts(targetPage, 20);
    page.value = targetPage;
  } catch (requestError) {
    error.value = requestError instanceof Error ? requestError.message : '内容列表加载失败';
  } finally {
    loading.value = false;
  }
}

async function confirmDelete(postId: string, title: string) {
  try {
    await ElMessageBox.confirm(`确认删除帖子「${title}」？删除后不可恢复。`, '删除确认', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }
  deleting.value = postId;
  try {
    await deleteAdminPost(postId);
    ElMessage.success('帖子已删除');
    await load(page.value);
  } catch (requestError) {
    ElMessage.error(requestError instanceof Error ? requestError.message : '删除失败');
  } finally {
    deleting.value = null;
  }
}

function formatDate(value: string) {
  return new Date(value).toLocaleString('zh-CN');
}

onMounted(async () => {
  if (!auth.initialized) await auth.loadMe();
  if (!isAdmin.value) {
    router.replace('/');
    return;
  }
  await load();
});
</script>

<template>
  <AppShell>
    <section class="admin-page">
      <header class="admin-hero">
        <p class="eyebrow">MODERATION</p>
        <h1>内容管理</h1>
        <p class="lead">审核并处理违规或过期的论坛帖子。</p>
      </header>

      <div v-if="loading" class="state-panel">正在加载内容…</div>
      <div v-else-if="error" class="state-panel error-panel">
        <strong>{{ error }}</strong>
        <button type="button" @click="load()">重试</button>
      </div>
      <div v-else-if="pageData.items.length === 0" class="state-panel">
        <strong>暂无待管理内容</strong>
        <span>当前没有已发布的帖子。</span>
      </div>
      <div v-else class="post-table">
        <div class="table-head">
          <span>帖子</span>
          <span>作者</span>
          <span>发布时间</span>
          <span>操作</span>
        </div>
        <article v-for="post in pageData.items" :key="post.id" class="table-row">
          <div class="post-cell">
            <router-link :to="`/forum/post/${post.id}`">{{ post.title }}</router-link>
            <span class="category">{{ post.category.name }}</span>
          </div>
          <span>{{ post.author.nickname || post.author.username }}</span>
          <time :datetime="post.createdAt">{{ formatDate(post.createdAt) }}</time>
          <button
            type="button"
            class="delete-button"
            :data-testid="`delete-post-${post.id}`"
            :disabled="deleting === post.id"
            @click="confirmDelete(post.id, post.title)"
          >{{ deleting === post.id ? '删除中…' : '删除' }}</button>
        </article>
      </div>

      <div v-if="!loading && pageData.totalPages > 1" class="pagination">
        <button type="button" :disabled="page <= 1" @click="load(page - 1)">上一页</button>
        <span>第 {{ page }} / {{ pageData.totalPages }} 页（共 {{ pageData.total }} 篇）</span>
        <button type="button" :disabled="page >= pageData.totalPages" @click="load(page + 1)">下一页</button>
      </div>
    </section>
  </AppShell>
</template>

<style scoped>
.admin-page { max-width: 1080px; margin: 0 auto; padding: 34px clamp(18px, 4vw, 48px) 72px; }
.admin-hero { padding: 24px 0 28px; border-bottom: 1px solid #edf0f8; }
.eyebrow { margin: 0 0 8px; color: #d92d20; font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0; color: #172033; font-size: clamp(28px, 5vw, 42px); letter-spacing: -.03em; }
.lead { margin: 12px 0 0; color: #667085; }
.state-panel { display: flex; align-items: center; justify-content: center; min-height: 220px; flex-direction: column; gap: 10px; border: 1px dashed #d0d5dd; border-radius: 14px; color: #667085; background: #fbfcff; }
.state-panel strong { color: #344054; font-size: 18px; }
.state-panel button { padding: 10px 16px; border: 0; border-radius: 9px; color: #fff; background: #4056ed; font: inherit; font-weight: 700; cursor: pointer; }
.error-panel { border-color: #fecdca; background: #fffbfa; }
.post-table { margin-top: 24px; overflow: hidden; border: 1px solid #edf0f8; border-radius: 14px; background: #fff; }
.table-head, .table-row { display: grid; grid-template-columns: minmax(240px, 2fr) 1fr 1.3fr 90px; gap: 16px; align-items: center; padding: 14px 18px; }
.table-head { color: #98a2b3; font-size: 12px; font-weight: 700; background: #f8faff; }
.table-row { border-top: 1px solid #edf0f8; color: #475467; font-size: 14px; }
.table-row:hover { background: #fbfcff; }
.post-cell { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.post-cell a { overflow: hidden; color: #172033; font-weight: 700; text-decoration: none; text-overflow: ellipsis; white-space: nowrap; }
.post-cell a:hover { color: #4056ed; }
.category { color: #4056ed; font-size: 12px; }
.delete-button { padding: 8px 14px; border: 1px solid #fecdca; border-radius: 8px; color: #d92d20; background: #fffbfa; font: inherit; font-weight: 700; cursor: pointer; }
.delete-button:disabled { opacity: .6; cursor: not-allowed; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 16px; margin-top: 26px; color: #667085; }
.pagination button { padding: 9px 14px; border: 0; border-radius: 9px; color: #fff; background: #4056ed; font: inherit; cursor: pointer; }
.pagination button:disabled { cursor: not-allowed; opacity: .45; }
@media (max-width: 720px) {
  .table-head { display: none; }
  .table-row { display: flex; flex-wrap: wrap; gap: 10px; padding: 16px; }
}
</style>
