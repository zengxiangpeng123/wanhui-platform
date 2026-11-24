<script setup lang="ts">
import { ref } from 'vue';
import type { CatalogResource } from '../api/resources';

const props = defineProps<{ resource: CatalogResource }>();
const iconFailed = ref(false);

function markIconFailed() {
  iconFailed.value = true;
}
</script>

<template>
  <article class="resource-card">
    <div class="resource-icon" :class="{ fallback: iconFailed || !props.resource.icon }">
      <img v-if="props.resource.icon && !iconFailed" :src="props.resource.icon" :alt="props.resource.name" @error="markIconFailed" />
      <span v-else>{{ props.resource.name.slice(0, 1) }}</span>
    </div>
    <div class="resource-content">
      <div class="resource-heading">
        <h3>{{ props.resource.name }}</h3>
        <span class="resource-category">{{ props.resource.subcategory }}</span>
      </div>
      <p>{{ props.resource.description }}</p>
      <div class="resource-footer">
        <a class="visit-button" :href="props.resource.url" target="_blank" rel="noopener noreferrer" @click.stop>访问网站</a>
      </div>
    </div>
  </article>
</template>

<style scoped>
.resource-card { display: flex; gap: 16px; min-height: 168px; padding: 18px; border: 1px solid #edf0f8; border-radius: 14px; background: #fff; transition: border-color .18s ease, box-shadow .18s ease, transform .18s ease; }
.resource-card:hover { border-color: #c7d0ff; box-shadow: 0 12px 30px rgba(64, 86, 237, .1); transform: translateY(-2px); }
.resource-icon { display: grid; flex: 0 0 48px; place-items: center; width: 48px; height: 48px; overflow: hidden; border-radius: 12px; background: #f4f6ff; color: #4056ed; font-size: 20px; font-weight: 800; }
.resource-icon img { width: 100%; height: 100%; object-fit: cover; }
.resource-content { display: flex; flex: 1; min-width: 0; flex-direction: column; }
.resource-heading { display: flex; align-items: baseline; gap: 10px; }
h3 { margin: 0; color: #172033; font-size: 17px; }
.resource-category { overflow: hidden; color: #98a2b3; font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }
p { flex: 1; margin: 10px 0 14px; color: #667085; font-size: 14px; line-height: 1.65; }
.resource-footer { display: flex; justify-content: flex-end; }
.visit-button { color: #4056ed; font-size: 14px; font-weight: 700; text-decoration: none; }
.visit-button:hover { text-decoration: underline; }
</style>
