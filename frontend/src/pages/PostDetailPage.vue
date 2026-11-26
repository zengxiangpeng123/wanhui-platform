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
    <section class="detail-page">
      <div v-if="loading" class="state-panel">正在加载帖子…</div>
      <div v-else-if="error && !post" class="state-panel">
        <strong>{{ error }}</strong>
        <router-link to="/forum">返回论坛</router-link>
      </div>
      <article v-else-if="post" class="post-detail">
        <router-link class="back-link" to="/forum">← 返回论坛</router-link>
        <header class="detail-header">
          <span class="category">{{ post.category.name }}</span>
          <h1>{{ post.title }}</h1>
          <div class="detail-meta">
            <span class="author">{{ post.author.nickname || post.author.username }}</span>
            <time :datetime="post.createdAt">{{ formatDate(post.createdAt) }}</time>
            <span>浏览 {{ post.viewCount }}</span>
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
.detail-page { max-width: 860px; margin: 0 auto; padding: 34px clamp(18px, 4vw, 48px) 72px; }
.state-panel { display: flex; align-items: center; justify-content: center; min-height: 240px; flex-direction: column; gap: 12px; border: 1px dashed #d0d5dd; border-radius: 14px; color: #667085; background: #fbfcff; }
.state-panel strong { color: #344054; font-size: 18px; }
.state-panel a { color: #4056ed; font-weight: 700; text-decoration: none; }
.back-link { display: inline-block; margin-bottom: 18px; color: #667085; text-decoration: none; }
.back-link:hover { color: #4056ed; }
.detail-header { padding-bottom: 20px; border-bottom: 1px solid #edf0f8; }
.category { display: inline-block; padding: 3px 10px; border-radius: 999px; color: #4056ed; background: #f1f3ff; font-size: 12px; }
h1 { margin: 12px 0; color: #172033; font-size: clamp(26px, 4vw, 38px); line-height: 1.3; letter-spacing: -.03em; }
.detail-meta { display: flex; flex-wrap: wrap; gap: 14px; color: #98a2b3; font-size: 13px; }
.author { color: #344054; font-weight: 600; }
.tags { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
.tag { color: #667085; font-size: 12px; }
.post-body { padding: 28px 0; color: #344054; font-size: 16px; line-height: 1.9; white-space: pre-wrap; }
.interaction-bar { display: flex; gap: 12px; padding: 18px 0; border-top: 1px solid #edf0f8; border-bottom: 1px solid #edf0f8; }
.interaction-button { padding: 9px 16px; border: 1px solid #d0d5dd; border-radius: 999px; color: #475467; background: #fff; font: inherit; cursor: pointer; }
.interaction-button:hover { border-color: #c7d0ff; color: #4056ed; }
.interaction-button.active { border-color: #4056ed; color: #4056ed; background: #f1f3ff; font-weight: 700; }
.interaction-button:disabled { cursor: not-allowed; opacity: .6; }
.comments-section { margin-top: 28px; }
.comments-section h2 { margin: 0 0 14px; color: #172033; font-size: 19px; }
.comment-editor { margin-bottom: 18px; }
.comment-editor textarea { width: 100%; padding: 12px; border: 1px solid #d0d5dd; border-radius: 10px; font: inherit; line-height: 1.6; resize: vertical; box-sizing: border-box; }
.comment-editor textarea:focus { border-color: #4056ed; outline: none; box-shadow: 0 0 0 3px rgba(64, 86, 237, .12); }
.comment-actions { display: flex; align-items: center; justify-content: flex-end; gap: 12px; margin-top: 10px; }
.comment-actions button { padding: 9px 16px; border: 0; border-radius: 9px; color: #fff; background: #4056ed; font: inherit; font-weight: 700; cursor: pointer; }
.comment-actions button:disabled { opacity: .6; cursor: not-allowed; }
.field-error { color: #d92d20; font-size: 12px; }
</style>


