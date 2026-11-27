<script setup lang="ts">
import AppShell from './AppShell.vue';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();

const links = [
  { to: '/user', label: '概览' },
  { to: '/user/profile', label: '个人资料' },
  { to: '/user/settings', label: '账号安全' },
  { to: '/user/favorites', label: '资源收藏' },
  { to: '/user/recent-visits', label: '最近访问' },
  { to: '/user/sites', label: '我的网站' }
];
</script>

<template>
  <AppShell>
    <template #sidebar>
      <aside class="user-nav">
        <p class="user-name">{{ auth.user?.nickname || auth.user?.username }}</p>
        <nav aria-label="个人中心导航">
          <router-link v-for="link in links" :key="link.to" :to="link.to">{{ link.label }}</router-link>
        </nav>
      </aside>
    </template>
    <slot />
  </AppShell>
</template>

<style scoped>
.user-nav { padding: 28px 16px; }
.user-name { margin: 0 0 14px; padding-left: 10px; overflow: hidden; color: #172033; font-weight: 800; text-overflow: ellipsis; white-space: nowrap; }
nav { display: flex; flex-direction: column; gap: 4px; }
nav a { padding: 10px; border-radius: 9px; color: #667085; text-decoration: none; }
nav a:hover { color: #4056ed; background: #f4f6ff; }
nav a.router-link-exact-active { color: #4056ed; background: #f1f3ff; font-weight: 700; }
@media (max-width: 760px) {
  .user-nav { display: flex; align-items: center; gap: 10px; overflow-x: auto; padding: 12px 16px; scrollbar-width: none; }
  .user-nav::-webkit-scrollbar { display: none; }
  .user-name { display: none; }
  nav { flex-direction: row; gap: 4px; }
  nav a { flex: 0 0 auto; padding: 8px 12px; white-space: nowrap; }
}
</style>
