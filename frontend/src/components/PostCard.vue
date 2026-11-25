<script setup lang="ts">
import type { PostSummary } from '../api/forum';

defineProps<{ post: PostSummary }>();

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('zh-CN');
}
</script>

<template>
  <article class="post-card">
    <router-link class="post-title" :to="`/forum/post/${post.id}`">{{ post.title }}</router-link>
    <p class="post-excerpt">{{ post.excerpt }}</p>
    <div class="post-meta">
      <span class="author">{{ post.author.nickname || post.author.username }}</span>
      <span class="category">{{ post.category.name }}</span>
      <time :datetime="post.createdAt">{{ formatDate(post.createdAt) }}</time>
      <span class="stat">评论 {{ post.commentCount }}</span>
      <span class="stat">点赞 {{ post.likeCount }}</span>
    </div>
    <div v-if="post.tags.length" class="post-tags">
      <span v-for="tag in post.tags" :key="tag" class="tag">#{{ tag }}</span>
    </div>
  </article>
</template>

<style scoped>
.post-card { display: flex; flex-direction: column; gap: 10px; padding: 20px; border: 1px solid #edf0f8; border-radius: 14px; background: #fff; transition: border-color .18s ease, box-shadow .18s ease, transform .18s ease; }
.post-card:hover { border-color: #c7d0ff; box-shadow: 0 12px 30px rgba(64, 86, 237, .1); transform: translateY(-2px); }
.post-title { color: #172033; font-size: 18px; font-weight: 700; text-decoration: none; }
.post-title:hover { color: #4056ed; }
.post-excerpt { margin: 0; color: #667085; font-size: 14px; line-height: 1.7; }
.post-meta { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; color: #98a2b3; font-size: 12px; }
.author { color: #344054; font-weight: 600; }
.category { padding: 2px 8px; border-radius: 999px; color: #4056ed; background: #f1f3ff; }
.post-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.tag { color: #667085; font-size: 12px; }
</style>
