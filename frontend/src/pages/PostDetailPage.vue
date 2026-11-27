<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  createComment,
  fetchComments,
  fetchPostDetail,
  setPostFavorite,
  setPostLike,
  type Comment,
  type PostDetail
} from '../api/forum';
import AppShell from '../layouts/AppShell.vue';
import CommentList from '../components/CommentList.vue';
import { useAuthStore } from '../stores/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const post = ref<PostDetail | null>(null);
const comments = ref<Comment[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const commentsLoading = ref(true);
const commentsError = ref<string | null>(null);
const commentText = ref('');
const commentError = ref<string | null>(null);
const commentSubmitting = ref(false);
const interactionPending = ref(false);

const postId = String(route.params.id);

function readStatus(requestError: unknown) {
  return (requestError as { status?: number | null }).status ?? null;
}

async function loadPost() {
  loading.value = true;
  error.value = null;
  try {
    post.value = await fetchPostDetail(postId);
  } catch (requestError) {
    const status = readStatus(requestError);
    if (status === 410) {
      error.value = '帖子已删除';
    } else if (status === 404) {
      error.value = '帖子不存在';
    } else {
      error.value = requestError instanceof Error ? requestError.message : '帖子加载失败';
    }
    post.value = null;
  } finally {
    loading.value = false;
  }
}

async function loadComments() {
  commentsLoading.value = true;
  commentsError.value = null;
  try {
    const page = await fetchComments(postId, 1, 20);
    comments.value = page.items;
  } catch (requestError) {
    commentsError.value = requestError instanceof Error ? requestError.message : '评论加载失败';
  } finally {
    commentsLoading.value = false;
  }
}

function requireLogin() {
  if (!auth.isAuthenticated) {
    router.push({ path: '/login', query: { redirect: route.fullPath } });
    return false;
  }
  return true;
}

async function toggleLike() {
  if (!post.value || !requireLogin() || interactionPending.value) return;
  interactionPending.value = true;
  const nextLiked = !post.value.liked;
  try {
    const state = await setPostLike(postId, nextLiked);
    post.value = {
      ...post.value,
      liked: state.liked ?? nextLiked,
      likeCount: state.likeCount ?? post.value.likeCount
    };
  } catch (requestError) {
    error.value = requestError instanceof Error ? requestError.message : '点赞失败';
  } finally {
    interactionPending.value = false;
  }
}

async function toggleFavorite() {
  if (!post.value || !requireLogin() || interactionPending.value) return;
  interactionPending.value = true;
  const nextFavorited = !post.value.favorited;
  try {
    const state = await setPostFavorite(postId, nextFavorited);
    post.value = {
      ...post.value,
      favorited: state.favorited ?? nextFavorited,
      favoriteCount: state.favoriteCount ?? post.value.favoriteCount
    };
  } catch (requestError) {
    error.value = requestError instanceof Error ? requestError.message : '收藏失败';
  } finally {
    interactionPending.value = false;
  }
}

async function submitComment() {
  if (!requireLogin()) return;
  const content = commentText.value.trim();
  if (!content) {
    commentError.value = '请输入评论内容';
    return;
  }
  if (content.length > 1000) {
    commentError.value = '评论不能超过 1000 字';
    return;
  }
  commentSubmitting.value = true;
  commentError.value = null;
  try {
    const created = await createComment(postId, content);
    comments.value = [created, ...comments.value];
    if (post.value) {
      post.value = { ...post.value, commentCount: post.value.commentCount + 1 };
    }
    commentText.value = '';
  } catch (requestError) {
    commentError.value = requestError instanceof Error ? requestError.message : '评论提交失败';
  } finally {
    commentSubmitting.value = false;
  }
}

function formatDate(value: string) {
  return new Date(value).toLocaleString('zh-CN');
}

onMounted(async () => {
  if (!auth.initialized) await auth.loadMe();
  await loadPost();
  if (post.value) await loadComments();
});
</script>

<template>
  <AppShell>
    <template #sidebar>
      <div class="detail-sidebar">
        <section class="detail-side-card">
          <router-link class="side-back" to="/forum">← 返回论坛</router-link>
          <h2>当前分类</h2>
          <p>{{ post?.category.name || '帖子' }}</p>
        </section>
        <section class="detail-side-card">
          <h2>互动提示</h2>
          <p>尊重不同观点，禁止发布广告和违规内容。友善讨论能让社区更有价值。</p>
        </section>
      </div>
    </template>
    <section class="detail-page">
      <div v-if="loading" class="state-panel">正在加载帖子…</div>
      <div v-else-if="error && !post" class="state-panel">
        <strong>{{ error }}</strong>
        <router-link to="/forum">返回论坛</router-link>
      </div>
      <article v-else-if="post" class="post-detail">
        <nav class="breadcrumb" aria-label="面包屑">
          <router-link to="/forum">论坛</router-link>
          <span>/</span>
          <span>{{ post.category.name }}</span>
        </nav>
        <header class="detail-header">
          <h1>{{ post.title }}</h1>
          <div class="author-line">
            <span class="author-avatar" aria-hidden="true">{{ (post.author.nickname || post.author.username).slice(0, 1) }}</span>
            <div>
              <strong>{{ post.author.nickname || post.author.username }}</strong>
              <div class="detail-meta">
                <time :datetime="post.createdAt">{{ formatDate(post.createdAt) }}</time>
                <span>浏览 {{ post.viewCount }}</span>
              </div>
            </div>
          </div>
          <div v-if="post.tags.length" class="tags">
            <span v-for="tag in post.tags" :key="tag" class="tag">#{{ tag }}</span>
          </div>
        </header>

        <div class="post-body">{{ post.content }}</div>

        <div class="interaction-bar">
          <button
            type="button"
            class="interaction-button"
            :class="{ active: post.liked }"
            data-testid="toggle-like"
            :disabled="interactionPending"
            @click="toggleLike"
          >
            {{ post.liked ? '已点赞' : '点赞' }} {{ post.likeCount }}
          </button>
          <button
            type="button"
            class="interaction-button"
            :class="{ active: post.favorited }"
            data-testid="toggle-favorite"
            :disabled="interactionPending"
            @click="toggleFavorite"
          >
            {{ post.favorited ? '已收藏' : '收藏' }} {{ post.favoriteCount }}
          </button>
        </div>

        <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />

        <section class="comments-section">
          <h2>评论 {{ post.commentCount }}</h2>

          <div class="comment-editor">
            <textarea
              v-model="commentText"
              data-testid="comment-input"
              rows="3"
              maxlength="1000"
              placeholder="写下你的看法…"
            ></textarea>
            <div class="comment-actions">
              <span v-if="commentError" class="field-error">{{ commentError }}</span>
              <button
                type="button"
                data-testid="comment-submit"
                :disabled="commentSubmitting"
                @click="submitComment"
              >{{ commentSubmitting ? '提交中…' : '发表评论' }}</button>
            </div>
          </div>

          <CommentList
            :comments="comments"
            :loading="commentsLoading"
            :error="commentsError"
            @unauthorized="router.push({ path: '/login', query: { redirect: route.fullPath } })"
            @like-error="error = $event"
          />
        </section>
      </article>
    </section>
  </AppShell>
</template>

<style scoped>
.detail-sidebar { display: grid; gap: 15px; }
.detail-side-card { padding: 18px; border: 1px solid #dfe6e1; border-radius: var(--radius); background: #fff; box-shadow: var(--shadow-sm); }
.side-back { display: inline-block; margin-bottom: 14px; color: var(--brand); font-size: 13px; font-weight: 650; text-decoration: none; }
.detail-side-card h2 { margin: 0 0 8px; color: var(--ink); font-size: 14px; }
.detail-side-card p { margin: 0; color: var(--muted); font-size: 13px; line-height: 1.7; }
.detail-page { min-width: 0; }
.state-panel { display: flex; align-items: center; justify-content: center; min-height: 240px; flex-direction: column; gap: 12px; padding: 24px; border: 1px dashed #cbd6d0; border-radius: 16px; color: var(--muted); background: rgba(255, 255, 255, .72); }
.state-panel strong { color: var(--ink); font-size: 18px; }
.state-panel a { color: var(--brand); font-weight: 700; text-decoration: none; }
.post-detail { padding: 30px; border: 1px solid #dfe6e1; border-radius: 16px; background: #fff; box-shadow: var(--shadow-sm); }
.breadcrumb { display: flex; gap: 7px; align-items: center; color: #7e8984; font-size: 13px; }
.breadcrumb a { color: var(--brand); text-decoration: none; }
.detail-header { padding: 14px 0 20px; border-bottom: 1px solid #e4ebe7; }
h1 { margin: 0 0 16px; color: var(--ink); font-size: clamp(26px, 3vw, 34px); line-height: 1.35; letter-spacing: -.025em; }
.author-line { display: flex; align-items: center; gap: 10px; }
.author-avatar { display: grid; width: 38px; height: 38px; place-items: center; border-radius: 50%; color: #173f38; background: #e7f1ed; font-weight: 800; }
.author-line strong { color: #33413c; font-size: 14px; }
.detail-meta { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 3px; color: #87928c; font-size: 12.5px; }
.tags { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
.tag { padding: 4px 8px; border-radius: 999px; color: #64716b; background: #eff2ef; font-size: 12px; }
.post-body { padding: 28px 0; color: #35433e; font-size: 16px; line-height: 1.9; white-space: pre-wrap; }
.interaction-bar { display: flex; gap: 12px; padding: 18px 0; border-top: 1px solid #e4ebe7; border-bottom: 1px solid #e4ebe7; }
.interaction-button { padding: 9px 16px; border: 1px solid #d5ded8; border-radius: 999px; color: #58655f; background: #fff; }
.interaction-button:hover { border-color: #9bbbb0; color: var(--brand); }
.interaction-button.active { border-color: var(--brand); color: var(--brand); background: #e7f1ed; font-weight: 700; }
.interaction-button:disabled { cursor: not-allowed; opacity: .6; }
.comments-section { margin-top: 28px; }
.comments-section h2 { margin: 0 0 14px; color: var(--ink); font-size: 19px; }
.comment-editor { margin-bottom: 18px; }
.comment-editor textarea { width: 100%; padding: 12px; border: 1px solid #d5ded8; border-radius: 10px; line-height: 1.6; resize: vertical; }
.comment-editor textarea:focus { border-color: #75a99a; outline: none; box-shadow: 0 0 0 4px rgba(23, 107, 91, .09); }
.comment-actions { display: flex; align-items: center; justify-content: flex-end; gap: 12px; margin-top: 10px; }
.comment-actions button { padding: 9px 16px; border: 0; border-radius: 9px; color: #fff; background: var(--brand); font-weight: 700; }
.comment-actions button:disabled { opacity: .6; cursor: not-allowed; }
.field-error { color: var(--danger); font-size: 12px; }
@media (max-width: 820px) {
  .detail-sidebar { display: none; }
  .post-detail { padding: 20px; }
}
</style>