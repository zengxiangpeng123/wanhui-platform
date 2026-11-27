<script setup lang="ts">
import { ref } from 'vue';
import { setCommentLike, type Comment } from '../api/forum';

const props = defineProps<{
  comments: Comment[];
  loading: boolean;
  error: string | null;
}>();

const emit = defineEmits<{
  unauthorized: [];
  'like-error': [message: string];
}>();

const pendingId = ref<string | null>(null);

function formatDate(value: string) {
  return new Date(value).toLocaleString('zh-CN');
}

async function toggleLike(comment: Comment) {
  if (pendingId.value) return;
  const previousLiked = comment.liked;
  const previousCount = comment.likeCount;
  const next = !previousLiked;

  comment.liked = next;
  comment.likeCount = Math.max(previousCount + (next ? 1 : -1), 0);
  pendingId.value = comment.id;
  try {
    const state = await setCommentLike(comment.id, next);
    comment.liked = state.liked ?? next;
    comment.likeCount = state.likeCount ?? comment.likeCount;
  } catch (requestError) {
    comment.liked = previousLiked;
    comment.likeCount = previousCount;
    const typed = requestError as { status?: number | null; message?: string };
    if (typed.status === 401) emit('unauthorized');
    else emit('like-error', typed.message || '点赞失败，请稍后重试');
  } finally {
    pendingId.value = null;
  }
}
</script>

<template>
  <section class="comment-list" aria-label="评论列表">
    <div v-if="loading" class="comment-state">评论加载中…</div>
    <div v-else-if="error" class="comment-state error">{{ error }}</div>
    <div v-else-if="props.comments.length === 0" class="comment-state">还没有评论，来发表第一条吧</div>
    <ul v-else>
      <li v-for="comment in props.comments" :key="comment.id" class="comment-item">
        <div class="comment-head">
          <strong>{{ comment.author.nickname || comment.author.username }}</strong>
          <time :datetime="comment.createdAt">{{ formatDate(comment.createdAt) }}</time>
        </div>
        <p>{{ comment.content }}</p>
        <div class="comment-actions">
          <button
            type="button"
            class="like-button"
            :class="{ active: comment.liked }"
            :data-testid="`comment-like-${comment.id}`"
            :disabled="pendingId === comment.id"
            :aria-pressed="comment.liked"
            @click="toggleLike(comment)"
          >
            {{ comment.liked ? '已赞' : '赞' }} {{ comment.likeCount }}
          </button>
        </div>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.comment-list { margin-top: 18px; }
.comment-state { padding: 24px; border: 1px dashed #d5ded8; border-radius: 12px; color: var(--muted); text-align: center; background: #f8faf8; }
.comment-state.error { color: var(--danger); border-color: #fecdca; background: #fffbfa; }
ul { display: flex; flex-direction: column; gap: 12px; margin: 0; padding: 0; list-style: none; }
.comment-item { padding: 16px; border: 1px solid var(--line); border-radius: 12px; background: #fff; }
.comment-head { display: flex; align-items: baseline; justify-content: space-between; gap: 12px; color: var(--ink-strong); }
.comment-head time { color: #87928c; font-size: 12px; }
.comment-item p { margin: 10px 0 0; color: #59655f; line-height: 1.7; white-space: pre-wrap; }
.comment-actions { display: flex; justify-content: flex-end; margin-top: 10px; }
.like-button { padding: 4px 12px; border: 1px solid #d5ded8; border-radius: 999px; color: var(--muted); background: #fff; font: inherit; font-size: 13px; cursor: pointer; }
.like-button:hover { border-color: #b7cec5; color: var(--brand); }
.like-button.active { border-color: var(--brand); color: var(--brand); background: #e7f1ed; font-weight: 700; }
.like-button:disabled { opacity: .6; cursor: not-allowed; }
</style>
