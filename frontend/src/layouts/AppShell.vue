<script setup lang="ts">
import AppHeader from '../components/AppHeader.vue';
</script>

<template>
  <div class="app-frame">
    <AppHeader />
    <div class="app-body">
      <aside v-if="$slots.sidebar" class="app-sidebar-slot">
        <slot name="sidebar" />
      </aside>
      <main class="app-main">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-frame { min-height: 100vh; background: linear-gradient(180deg, #f8faff 0%, #ffffff 44%); }
.app-body { display: grid; grid-template-columns: 220px minmax(0, 1fr); min-height: calc(100vh - 76px); }
.app-sidebar-slot { min-width: 0; }
.app-main { min-width: 0; }
@media (max-width: 760px) { .app-body { display: block; } .app-sidebar-slot { border-bottom: 1px solid #edf0f8; } }
</style>
