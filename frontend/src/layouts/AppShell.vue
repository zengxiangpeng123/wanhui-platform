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
        <footer class="site-footer">
          <span>© 2026 万汇网</span>
          <nav aria-label="站点信息">
            <router-link to="/about">关于</router-link>
            <router-link to="/privacy">隐私政策</router-link>
            <router-link to="/terms">服务条款</router-link>
          </nav>
        </footer>
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-frame { min-height: 100vh; background: linear-gradient(180deg, #f8faff 0%, #ffffff 44%); }
.app-body { display: grid; grid-template-columns: 220px minmax(0, 1fr); min-height: calc(100vh - 76px); }
.app-sidebar-slot { min-width: 0; }
.app-main { min-width: 0; }
.site-footer { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 12px; padding: 22px clamp(18px, 4vw, 48px); border-top: 1px solid #edf0f8; color: #98a2b3; font-size: 13px; }
.site-footer nav { display: flex; gap: 16px; }
.site-footer a { color: #667085; text-decoration: none; }
.site-footer a:hover { color: #4056ed; }
@media (max-width: 760px) { .app-body { display: block; } .app-sidebar-slot { border-bottom: 1px solid #edf0f8; } }
</style>

