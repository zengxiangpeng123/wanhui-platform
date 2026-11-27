<script setup lang="ts">
import AppHeader from '../components/AppHeader.vue';
</script>

<template>
  <div class="app-frame">
    <AppHeader />
    <div class="app-body" :class="$slots.sidebar ? 'app-body--with-sidebar' : 'app-body--full'">
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
.app-frame { min-height: 100vh; background: var(--page-bg); }
.app-body { width: min(1400px, 100%); min-height: calc(100vh - 75px); margin: 0 auto; padding: 24px; }
.app-body--with-sidebar { display: grid; grid-template-columns: 270px minmax(0, 1fr); align-items: start; gap: 22px; }
.app-body--full { display: block; }
.app-sidebar-slot { min-width: 0; }
.app-main { min-width: 0; }
.site-footer { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 12px; margin-top: 28px; padding: 22px 0 4px; border-top: 1px solid var(--line); color: #8a958f; font-size: 13px; }
.site-footer nav { display: flex; gap: 16px; }
.site-footer a { color: var(--muted); text-decoration: none; }
.site-footer a:hover { color: var(--brand); }
@media (max-width: 820px) {
  .app-body { padding: 16px; }
  .app-body--with-sidebar { display: block; }
  .app-sidebar-slot { overflow-x: auto; margin-bottom: 14px; }
}
</style>

