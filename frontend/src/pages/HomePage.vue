<script setup lang="ts">
import AppShell from '../layouts/AppShell.vue';
</script>

<template>
  <AppShell>
  <section class="page-card">
    <p class="eyebrow">WANHUI PLATFORM</p>
    <h1>万汇网</h1>
    <p class="lead">发现值得收藏的工具、资料与讨论。</p>
    <div class="actions">
      <router-link class="primary-link" to="/register">创建账号</router-link>
      <router-link class="secondary-link" to="/login">已有账号，登录</router-link>
    </div>
  </section>
  </AppShell>
</template>

<style scoped>
.page-card { max-width: 720px; margin: 0 auto; padding: 72px 24px; }
.eyebrow { margin: 0 0 12px; color: #4056ed; font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0; color: #172033; font-size: clamp(42px, 8vw, 72px); line-height: 1; }
.lead { margin: 22px 0 30px; color: #667085; font-size: 18px; }
.actions { display: flex; flex-wrap: wrap; gap: 12px; }
.primary-link, .secondary-link { padding: 12px 18px; border-radius: 10px; text-decoration: none; font-weight: 700; }
.primary-link { color: #fff; background: #4056ed; }
.secondary-link { color: #4056ed; background: #eef1ff; }
</style>
