<script setup lang="ts">
import { computed, ref } from 'vue';
import type { CatalogResource } from '../api/resources';

const props = defineProps<{ resource: CatalogResource; favoriting?: boolean }>();
const emit = defineEmits<{ 'toggle-favorite': [resource: CatalogResource] }>();
const iconFailed = ref(false);
const tagLabels: Record<string, string> = {
  design: '设计资源', 'design-tools': '设计工具', 'design-resources': '设计素材', 'design-inspiration': '设计灵感',
  ai: 'AI 工具', 'ai-image': 'AI 图像', 'ai-chatbots': 'AI 对话', 'ai-writing': 'AI 写作', 'ai-voice': 'AI 语音', 'ai-video': 'AI 视频', 'ai-code': 'AI 编程',
  academic: '学术资源', 'open-access': '开放获取', 'academic-journals': '学术期刊', 'academic-search': '学术检索', 'academic-openaccess': '开放学术', 'academic-papers': '学术论文', 'academic-writing': '学术写作', 'academic-reference': '文献管理',
  media: '媒体资源', 'media-music': '音乐', 'media-anime': '动漫', 'media-news': '新闻', 'media-series': '剧集', 'media-documentary': '纪录片', 'media-editing': '媒体编辑', 'media-movies': '电影'
};
const visibleTags = computed(() => (props.resource.tags?.length ? props.resource.tags.slice(0, 3) : [props.resource.subcategory]).map((tag) => tagLabels[tag] || tag));

function markIconFailed() {
  iconFailed.value = true;
}
</script>

<template>
  <article class="resource-card">
    <div class="resource-top">
      <div class="resource-icon" :class="{ fallback: iconFailed || !props.resource.icon }">
        <img v-if="props.resource.icon && !iconFailed" :src="props.resource.icon" :alt="props.resource.name" @error="markIconFailed" />
        <span v-else>{{ props.resource.name.slice(0, 1) }}</span>
      </div>
      <div class="resource-copy">
        <h3>{{ props.resource.name }}</h3>
        <p>{{ props.resource.description }}</p>
      </div>
    </div>

    <div class="tag-row">
      <span v-for="tag in visibleTags" :key="tag" class="tag">{{ tag }}</span>
    </div>

    <div class="resource-footer">
      <a class="visit-button" :href="props.resource.url" target="_blank" rel="noopener noreferrer" @click.stop>访问网站 <span aria-hidden="true">↗</span></a>
      <button
        type="button"
        class="favorite-button"
        :class="{ active: props.resource.favorited }"
        :aria-pressed="props.resource.favorited"
        :data-testid="`resource-favorite-${props.resource.key}`"
        :disabled="props.favoriting"
        @click="emit('toggle-favorite', props.resource)"
      >
        <span aria-hidden="true">{{ props.resource.favorited ? '♥' : '♡' }}</span>
        <span class="sr-only">{{ props.resource.favorited ? '已收藏' : '收藏' }}</span>
      </button>
    </div>
  </article>
</template>

<style scoped>
.resource-card { display: flex; min-height: 211px; flex-direction: column; padding: 20px; border: 1px solid #dfe6e1; border-radius: 16px; background: rgba(255, 255, 255, .94); box-shadow: var(--shadow-sm); transition: transform .2s ease, border-color .2s ease, box-shadow .2s ease; }
.resource-card:hover { transform: translateY(-3px); border-color: #b7cec5; box-shadow: 0 15px 30px rgba(30, 55, 45, .09); }
.resource-top { display: flex; align-items: flex-start; gap: 13px; }
.resource-icon { display: grid; flex: 0 0 auto; width: 48px; height: 48px; place-items: center; overflow: hidden; border: 1px solid #e4ebe7; border-radius: 13px; color: var(--brand); background: #f4f8f5; box-shadow: 0 5px 12px rgba(30, 55, 45, .06); font-size: 19px; font-weight: 800; }
.resource-icon img { width: 100%; height: 100%; object-fit: cover; }
.resource-copy { min-width: 0; }
h3 { margin: 0; color: var(--ink); font-size: 17px; line-height: 1.35; }
.resource-copy p { display: -webkit-box; margin: 6px 0 0; overflow: hidden; color: var(--muted); font-size: 13.5px; line-height: 1.65; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.tag-row { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 13px; }
.tag { padding: 4px 8px; border-radius: 999px; color: #5d6862; background: #eff2ef; font-size: 12px; }
.resource-footer { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-top: auto; padding-top: 16px; }
.visit-button { display: inline-flex; align-items: center; gap: 7px; min-height: 40px; padding: 0 14px; border-radius: 10px; color: #fff; background: var(--brand); box-shadow: 0 7px 16px rgba(23, 107, 91, .13); font-size: 13.5px; font-weight: 650; text-decoration: none; }
.visit-button:hover { background: var(--brand-dark); }
.favorite-button { display: grid; width: 42px; height: 42px; place-items: center; border: 1px solid #dfe6e1; border-radius: 50%; color: #85918b; background: #fff; font-size: 22px; line-height: 1; transition: .18s ease; }
.favorite-button:hover { color: var(--brand); border-color: #b7cec5; }
.favorite-button.active { color: #fff; border-color: transparent; background: var(--brand); box-shadow: 0 6px 14px rgba(23, 107, 91, .17); }
.favorite-button:disabled { cursor: not-allowed; opacity: .55; }
.sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0, 0, 0, 0); white-space: nowrap; border: 0; }
</style>