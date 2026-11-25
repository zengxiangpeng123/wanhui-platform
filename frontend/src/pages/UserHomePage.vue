<script setup lang="ts">
import { useAuthStore } from '../stores/auth';
import AppShell from '../layouts/AppShell.vue';
const auth = useAuthStore();
</script>

<template>
  <AppShell>
  <section class="page-card">
    <p class="eyebrow">ACCOUNT</p>
    <h1>个人中心</h1>
    <p class="lead">你好，{{ auth.user?.nickname || auth.user?.username }}。</p>
    <nav class="user-links">
      <router-link to="/user/favorites">资源收藏</router-link>
      <router-link to="/user/recent-visits">最近访问</router-link>
      <router-link to="/user/sites">我的网站</router-link>
    </nav>
    <router-link class="secondary-link" to="/">返回首页</router-link>
  </section>
  </AppShell>
</template>

<style scoped>
.page-card { max-width: 720px; margin: 0 auto; padding: 72px 24px; }
.eyebrow { margin: 0 0 12px; color: #4056ed; font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0; color: #172033; font-size: clamp(36px, 7vw, 60px); }
.lead { margin: 18px 0 28px; color: #667085; font-size: 18px; }
.user-links { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 22px; }
.user-links a { padding: 9px 13px; border-radius: 8px; color: #4056ed; background: #eef1ff; text-decoration: none; font-weight: 700; }
.secondary-link { display: inline-block; padding: 12px 18px; border-radius: 10px; color: #4056ed; background: #eef1ff; text-decoration: none; font-weight: 700; }
</style>
