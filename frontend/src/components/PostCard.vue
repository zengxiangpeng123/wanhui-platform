<script setup lang="ts">
import { computed } from 'vue';
import type { PostSummary } from '../api/forum';

const props = defineProps<{ post: PostSummary }>();
const authorName = computed(() => props.post.author.nickname || props.post.author.username);
const avatarText = computed(() => props.post.category.name.slice(0, 1));

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('zh-CN');
}
</script>

<template>
  <article class="post-card">
    <span class="avatar" aria-hidden="true">{{ avatarText }}</span>
    <div class="post-content">
      <h3><router-link :to="`/forum/post/${post.id}`">{{ post.title }}</router-link></h3>
      <div class="post-meta">
        <span class="author">{{ authorName }}</span>
        <time :datetime="post.createdAt">{{ formatDate(post.createdAt) }}</time>
        <span class="category">{{ post.category.name }}</span>
      </div>
      <p class="post-excerpt">{{ post.excerpt }}</p>
      <div class="post-stats">
        <span>浏览 {{ post.viewCount }}</span>
        <span>评论 {{ post.commentCount }}</span>
        <span>♡ 点赞 {{ post.likeCount }}</span>
        <span>☆ 收藏 {{ post.favoriteCount }}</span>
      </div>
      <div v-if="post.tags.length" class="post-tags">
        <span v-for="tag in post.tags" :key="tag" class="tag">#{{ tag }}</span>
      </div>
    </div>
  </article>
</template>

<style scoped>
.post-card { display: grid; grid-template-columns: 46px minmax(0, 1fr); gap: 14px; padding: 18px; border: 1px solid #dfe6e1; border-radius: 16px; background: rgba(255, 255, 255, .94); box-shadow: var(--shadow-sm); transition: transform .2s ease, border-color .2s ease, box-shadow .2s ease; }
.post-card:hover { transform: translateY(-2px); border-color: #bdd2c8; box-shadow: 0 12px 26px rgba(30, 55, 45, .075); }
.avatar { display: grid; width: 40px; height: 40px; place-items: center; border-radius: 50%; color: #173f38; background: #e7f1ed; font-size: 14px; font-weight: 800; }
.post-content { min-width: 0; }
h3 { margin: 0; }
h3 a { color: var(--ink); font-size: 17px; font-weight: 750; line-height: 1.4; text-decoration: none; }
h3 a:hover { color: var(--brand); }
.post-meta { display: flex; flex-wrap: wrap; gap: 7px 13px; margin-top: 7px; color: #7e8984; font-size: 12.5px; }
.author { color: #35433e; font-weight: 650; }
.category { padding: 3px 8px; border-radius: 999px; color: var(--brand); background: #e7f1ed; }
.post-excerpt { display: -webkit-box; margin: 9px 0 0; overflow: hidden; color: #63706a; font-size: 13.5px; line-height: 1.65; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.post-stats { display: flex; flex-wrap: wrap; gap: 16px; margin-top: 12px; color: #75807b; font-size: 13px; }
.post-tags { display: flex; flex-wrap: wrap; gap: 7px; margin-top: 10px; }
.tag { color: #68736e; font-size: 12px; }
@media (max-width: 640px) {
  .post-card { grid-template-columns: 1fr; }
  .avatar { width: 36px; height: 36px; }
  .post-stats { gap: 11px; }
}
</style>