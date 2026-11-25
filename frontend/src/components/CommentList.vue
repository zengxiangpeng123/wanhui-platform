<script setup lang="ts">
import type { Comment } from '../api/forum';

defineProps<{
  comments: Comment[];
  loading: boolean;
  error: string | null;
}>();

function formatDate(value: string) {
  return new Date(value).toLocaleString('zh-CN');
}
</script>

<template>
  <section class="comment-list" aria-label="评论列表">
    <div v-if="loading" class="comment-state">评论加载中…</div>
    <div v-else-if="error" class="comment-state error">{{ error }}</div>
    <div v-else-if="comments.length === 0" class="comment-state">还没有评论，来发表第一条吧</div>
    <ul v-else>
      <li v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-head">
          <strong>{{ comment.author.nickname || comment.author.username }}</strong>
          <time :datetime="comment.createdAt">{{ formatDate(comment.createdAt) }}</time>
        </div>
        <p>{{ comment.content }}</p>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.comment-list { margin-top: 18px; }
.comment-state { padding: 24px; border: 1px dashed #d0d5dd; border-radius: 12px; color: #667085; text-align: center; background: #fbfcff; }
.comment-state.error { color: #d92d20; border-color: #fecdca; background: #fffbfa; }
ul { display: flex; flex-direction: column; gap: 12px; margin: 0; padding: 0; list-style: none; }
.comment-item { padding: 16px; border: 1px solid #edf0f8; border-radius: 12px; background: #fff; }
.comment-head { display: flex; align-items: baseline; justify-content: space-between; gap: 12px; color: #344054; }
.comment-head time { color: #98a2b3; font-size: 12px; }
.comment-item p { margin: 10px 0 0; color: #475467; line-height: 1.7; white-space: pre-wrap; }
</style>
