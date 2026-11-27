<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { clearRecentVisits, fetchRecentVisits, type RecentVisit } from '../api/userResources';
import AppShell from '../layouts/AppShell.vue';
import EmptyState from '../components/EmptyState.vue';

const items = ref<RecentVisit[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

async function load() {
  loading.value = true;
  try {
    items.value = await fetchRecentVisits();
  } catch (requestError) {
    error.value = requestError instanceof Error ? requestError.message : '最近访问加载失败';
  } finally {
    loading.value = false;
  }
}

async function clear() {
  await clearRecentVisits();
  items.value = [];
}

onMounted(load);
</script>

<template>
  <AppShell>
    <section class="user-page">
      <router-link class="back-link" to="/user">返回个人中心</router-link>
      <div class="page-heading">
        <div><p class="eyebrow">RECENT TRAIL</p><h1>最近访问</h1></div>
        <button v-if="items.length" class="clear-button" type="button" @click="clear">清空记录</button>
      </div>
      <div v-if="loading" class="loading">正在加载最近访问…</div>
      <el-alert v-else-if="error" :title="error" type="error" :closable="false" show-icon />
      <EmptyState v-else-if="items.length === 0" title="暂无最近访问" description="打开资源后，最近访问会记录在这里。" />
      <div v-else class="visit-list">
        <article v-for="item in items" :key="item.resourceKey" class="visit-row">
          <strong>{{ item.resourceName }}</strong>
          <span>{{ item.resourceKey }}</span>
          <time>{{ item.visitedAt }}</time>
        </article>
      </div>
    </section>
  </AppShell>
</template>

<style scoped>
.user-page { max-width: 900px; margin: 0 auto; padding: 42px clamp(18px, 4vw, 48px) 72px; }
.back-link { color: var(--muted); font-size: 14px; text-decoration: none; }
.page-heading { display: flex; align-items: end; justify-content: space-between; gap: 16px; margin-top: 26px; }
.eyebrow { margin: 0 0 10px; color: var(--brand); font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0 0 26px; color: var(--ink); font-size: clamp(34px, 6vw, 54px); }
.loading { color: var(--muted); }
.clear-button { margin-bottom: 26px; padding: 9px 14px; border: 1px solid #d5ded8; border-radius: 8px; color: #59655f; background: #fff; font: inherit; cursor: pointer; }
.visit-list { border-top: 1px solid var(--line); }
.visit-row { display: grid; grid-template-columns: 1fr 140px 180px; gap: 16px; padding: 16px 0; border-bottom: 1px solid var(--line); color: var(--muted); }
.visit-row strong { color: var(--ink-strong); }
@media (max-width: 620px) { .visit-row { grid-template-columns: 1fr; gap: 5px; } }
</style>
