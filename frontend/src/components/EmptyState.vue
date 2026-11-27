<script setup lang="ts">
defineProps<{
  title: string;
  description: string;
}>();
</script>

<template>
  <div class="empty-state" data-testid="empty-state">
    <strong>{{ title }}</strong>
    <span>{{ description }}</span>
    <slot />
  </div>
</template>

<style scoped>
.empty-state { display: flex; min-height: 220px; align-items: center; justify-content: center; flex-direction: column; gap: 10px; padding: 32px; border: 1px dashed #d5ded8; border-radius: 14px; color: var(--muted); background: #f8faf8; text-align: center; }
.empty-state strong { color: var(--ink-strong); font-size: 18px; }
:slotted(button), :slotted(a) { margin-top: 8px; padding: 10px 15px; border: 0; border-radius: 8px; color: #fff; background: var(--brand); font: inherit; font-weight: 700; text-decoration: none; cursor: pointer; }
</style>
