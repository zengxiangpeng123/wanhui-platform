<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();
const searchText = ref('');
const displayName = computed(() => auth.user?.nickname || auth.user?.username || '');
const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase() || '游');

watch(
  () => route.query.q,
  (value) => {
    searchText.value = typeof value === 'string' ? value : '';
  },
  { immediate: true }
);

onMounted(() => {
  if (!auth.initialized) {
    auth.loadMe();
  }
});

function submitSearch() {
  const q = searchText.value.trim();
  router.push({ path: '/', query: q ? { q } : {} });
}

async function logout() {
  await auth.logout();
  await router.replace('/');
}
</script>

<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link class="brand" to="/" aria-label="万汇网首页">
        <span class="brand-mark" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="21" height="21" fill="none">
            <path d="M4 7h16M4 12h16M4 17h16" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
          </svg>
        </span>
        <span class="brand-copy">
          <span class="brand-name">万汇网</span>
          <span class="brand-sub">多领域资源聚合平台</span>
        </span>
      </router-link>

      <form class="header-search" role="search" @submit.prevent="submitSearch">
        <svg class="search-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" aria-hidden="true">
          <circle cx="11" cy="11" r="6.5" stroke="currentColor" stroke-width="1.8" />
          <path d="m16 16 4 4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
        </svg>
        <input v-model="searchText" data-testid="resource-search" aria-label="搜索资源" placeholder="搜索资源、工具或网站..." />
        <span class="search-tag">站内资源</span>
        <button class="search-go" type="submit" aria-label="搜索" data-testid="resource-search-submit">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" aria-hidden="true">
            <circle cx="11" cy="11" r="6" stroke="currentColor" stroke-width="2" />
            <path d="m15.5 15.5 4.5 4.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
          </svg>
        </button>
      </form>

      <nav class="header-actions" aria-label="主导航">
        <router-link class="header-btn forum-link" to="/forum">
          <span aria-hidden="true">▣</span>
          论坛
        </router-link>
        <template v-if="auth.isAuthenticated">
          <router-link v-if="auth.user?.roles.includes('ADMIN')" class="header-btn" to="/admin/content">内容管理</router-link>
          <router-link class="account-entry" to="/user" :aria-label="`${displayName}的个人中心`">
            <span class="account-avatar">{{ avatarText }}</span>
            <span class="account-name">{{ displayName }}</span>
          </router-link>
          <button class="logout-link" type="button" @click="logout">退出</button>
        </template>
        <router-link v-else class="header-login login-link" to="/login">登录</router-link>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.app-header { position: sticky; top: 0; z-index: 60; border-bottom: 1px solid var(--line); background: rgba(255, 255, 255, .94); box-shadow: 0 4px 18px rgba(30, 55, 45, .035); backdrop-filter: blur(18px); }
.header-inner { display: grid; grid-template-columns: 250px minmax(320px, 1fr) auto; align-items: center; gap: 20px; width: min(1400px, 100%); margin: 0 auto; padding: 13px 24px; }
.brand { display: flex; align-items: center; gap: 11px; min-width: 0; text-decoration: none; }
.brand-mark { display: grid; flex: 0 0 auto; width: 42px; height: 42px; place-items: center; border-radius: 13px; color: #fff; background: var(--brand); box-shadow: 0 8px 18px rgba(23, 107, 91, .16); }
.brand-copy { display: flex; min-width: 0; flex-direction: column; }
.brand-name { color: #173c35; font-size: 23px; font-weight: 850; line-height: 1; letter-spacing: -.03em; }
.brand-sub { margin-top: 4px; color: var(--muted); font-size: 12px; white-space: nowrap; }
.header-search { display: flex; align-items: center; gap: 10px; min-width: 0; height: 48px; padding: 4px 4px 4px 16px; border: 1px solid #d9e2dc; border-radius: 999px; background: #fff; box-shadow: 0 4px 14px rgba(30, 55, 45, .04); }
.search-icon { flex: 0 0 auto; color: #7f8f88; }
.header-search input { flex: 1; min-width: 0; border: 0; outline: 0; background: transparent; font-size: 14px; }
.header-search input::placeholder { color: #8b9691; }
.search-tag { flex: 0 0 auto; padding: 5px 9px; border-radius: 999px; color: var(--brand); background: #e7f1ed; font-size: 12px; }
.search-go { display: grid; flex: 0 0 auto; width: 40px; height: 40px; place-items: center; border: 0; border-radius: 50%; color: #fff; background: var(--brand); box-shadow: 0 7px 16px rgba(23, 107, 91, .13); }
.search-go:hover { background: var(--brand-dark); }
.header-actions { display: flex; align-items: center; gap: 8px; white-space: nowrap; }
.header-btn, .header-login { display: inline-flex; align-items: center; gap: 7px; min-height: 44px; padding: 0 14px; border: 1px solid transparent; border-radius: 999px; color: #48544f; text-decoration: none; transition: .18s ease; }
.header-btn:hover { color: var(--brand); background: #edf4f0; }
.header-login { color: #fff; background: var(--brand); box-shadow: 0 7px 16px rgba(23, 107, 91, .13); font-weight: 650; }
.header-login:hover { background: var(--brand-dark); }
.account-entry { display: flex; align-items: center; gap: 8px; min-height: 44px; padding: 4px 10px 4px 4px; border: 1px solid #dce4df; border-radius: 999px; background: #fff; text-decoration: none; }
.account-avatar { display: grid; width: 34px; height: 34px; place-items: center; border-radius: 50%; color: var(--brand); background: #e7f1ed; font-weight: 800; }
.account-name { max-width: 130px; overflow: hidden; color: #34423d; font-size: 14px; font-weight: 650; text-overflow: ellipsis; }
.logout-link { padding: 8px 6px; border: 0; color: #7b8580; background: transparent; }
.logout-link:hover { color: var(--danger); }
@media (max-width: 1060px) {
  .header-inner { grid-template-columns: 210px minmax(220px, 1fr) auto; gap: 12px; padding-inline: 18px; }
  .brand-sub, .search-tag, .account-name { display: none; }
}
@media (max-width: 760px) {
  .header-inner { grid-template-columns: 1fr auto; gap: 10px; padding: 11px 16px 12px; }
  .brand-mark { width: 40px; height: 40px; }
  .brand-name { font-size: 21px; }
  .header-search { grid-column: 1 / -1; grid-row: 2; height: 46px; }
  .header-actions { grid-column: 2; grid-row: 1; gap: 4px; }
  .header-btn { min-height: 40px; padding: 0 10px; }
  .account-entry { min-height: 40px; padding: 3px; }
}
</style>