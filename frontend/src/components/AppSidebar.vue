<script setup lang="ts">
export interface ResourceCategory {
  value: string;
  label: string;
}

defineProps<{
  activeCategory: string;
  categories: ResourceCategory[];
}>();

defineEmits<{
  select: [category: string];
}>();
</script>

<template>
  <nav class="resource-sidebar" aria-label="资源分类">
    <p class="sidebar-title">资源分类</p>
    <button
      v-for="item in categories"
      :key="item.value || 'all'"
      class="category-button"
      :data-category="item.value"
      :class="{ active: activeCategory === item.value }"
      type="button"
      @click="$emit('select', item.value)"
    >
      {{ item.label }}
    </button>
  </nav>
</template>

<style scoped>
.resource-sidebar { padding: 28px 18px; border-right: 1px solid #edf0f8; background: #fbfcff; }
.sidebar-title { margin: 0 10px 14px; color: #98a2b3; font-size: 12px; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; }
.category-button { display: block; width: 100%; margin: 4px 0; padding: 10px 12px; border: 0; border-radius: 9px; color: #475467; background: transparent; font: inherit; text-align: left; cursor: pointer; }
.category-button:hover, .category-button.active { color: #3444c8; background: #eef1ff; font-weight: 700; }
@media (max-width: 760px) {
  .resource-sidebar { display: flex; align-items: center; gap: 8px; overflow-x: auto; padding: 12px 16px; border-right: 0; scrollbar-width: none; }
  .resource-sidebar::-webkit-scrollbar { display: none; }
  .sidebar-title { display: none; }
  .category-button { flex: 0 0 auto; width: auto; margin: 0; padding: 8px 14px; border-radius: 999px; text-align: center; white-space: nowrap; }
}
</style>
