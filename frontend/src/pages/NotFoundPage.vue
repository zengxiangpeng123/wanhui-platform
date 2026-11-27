<script setup lang="ts">
import AppShell from '../layouts/AppShell.vue';
</script>

<template>
  <AppShell>
  <section class="page-card">
    <p class="eyebrow">404</p>
    <h1>页面不存在</h1>
    <p class="lead">这个地址没有对应的页面。</p>
    <router-link class="secondary-link" to="/">返回首页</router-link>
  </section>
  </AppShell>
</template>

<style scoped>
.page-card { max-width: 720px; margin: 0 auto; padding: 72px 24px; }
.eyebrow { margin: 0 0 12px; color: var(--brand); font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0; color: var(--ink); font-size: clamp(36px, 7vw, 60px); }
.lead { margin: 18px 0 28px; color: var(--muted); font-size: 18px; }
.secondary-link { display: inline-block; padding: 12px 18px; border-radius: 10px; color: var(--brand); background: #e7f1ed; text-decoration: none; font-weight: 700; }
</style>
