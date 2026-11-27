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

      <div class="admin-stats">
        <div class="stat"><small>正常帖子</small><strong>{{ pageData.total }}</strong></div>
        <div class="stat"><small>当前页</small><strong>{{ pageData.items.length }}</strong></div>
        <div class="stat"><small>审计写入</small><strong>启用</strong></div>
        <div class="stat"><small>当前角色</small><strong>管理员</strong></div>
      </div>

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
.admin-page { min-width: 0; }
.admin-hero { margin-bottom: 18px; }
.eyebrow { display: inline-flex; margin-bottom: 9px; padding: 5px 10px; border-radius: 999px; color: var(--brand); background: #e7f1ed; font-size: 12px; font-weight: 700; }
h1 { margin: 0; color: var(--ink); font-size: clamp(26px, 3vw, 32px); letter-spacing: -.025em; }
.lead { margin: 9px 0 0; color: var(--muted); font-size: 14px; }
.admin-stats { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; margin-bottom: 18px; }
.stat { padding: 18px; border: 1px solid #dfe6e1; border-radius: 14px; background: #fff; box-shadow: var(--shadow-sm); }
.stat small { display: block; color: var(--muted); font-size: 12px; }
.stat strong { display: block; margin-top: 9px; color: var(--ink); font-size: 24px; }
.state-panel { display: flex; align-items: center; justify-content: center; min-height: 220px; flex-direction: column; gap: 10px; border: 1px dashed #cbd6d0; border-radius: 16px; color: var(--muted); background: rgba(255, 255, 255, .72); }
.state-panel strong { color: var(--ink); font-size: 18px; }
.state-panel button { padding: 9px 14px; border: 0; border-radius: 9px; color: #fff; background: var(--brand); font-weight: 650; }
.error-panel { border-color: #f0c7c7; background: #fffafa; }
.post-table { margin-top: 20px; overflow: hidden; border: 1px solid #dfe6e1; border-radius: 14px; background: #fff; box-shadow: var(--shadow-sm); }
.table-head, .table-row { display: grid; grid-template-columns: minmax(240px, 2fr) 1fr 1.3fr 90px; gap: 16px; align-items: center; padding: 14px 18px; }
.table-head { color: #77827d; background: #f4f6f3; font-size: 12px; font-weight: 700; }
.table-row { border-top: 1px solid #e4ebe7; color: #4c5953; font-size: 14px; }
.table-row:hover { background: #f8faf8; }
.post-cell { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.post-cell a { overflow: hidden; color: var(--ink); font-weight: 700; text-decoration: none; text-overflow: ellipsis; white-space: nowrap; }
.post-cell a:hover { color: var(--brand); }
.category { color: var(--brand); font-size: 12px; }
.delete-button { padding: 8px 12px; border: 1px solid #f1caca; border-radius: 8px; color: var(--danger); background: #fffafa; font-weight: 650; }
.delete-button:disabled { opacity: .6; cursor: not-allowed; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 16px; margin-top: 24px; color: var(--muted); font-size: 13px; }
.pagination button { padding: 9px 14px; border: 0; border-radius: 9px; color: #fff; background: var(--brand); }
.pagination button:disabled { cursor: not-allowed; opacity: .45; }
@media (max-width: 980px) { .admin-stats { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 720px) {
  .admin-stats { grid-template-columns: 1fr 1fr; }
  .table-head { display: none; }
  .table-row { display: flex; flex-wrap: wrap; gap: 10px; padding: 16px; }
}
</style>