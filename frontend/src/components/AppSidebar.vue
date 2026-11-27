<script setup lang="ts">
import { computed } from 'vue';
import { useAuthStore } from '../stores/auth';

export interface ResourceCategory {
  value: string;
  label: string;
}

const props = defineProps<{
  activeCategory: string;
  categories: ResourceCategory[];
}>();

defineEmits<{
  select: [category: string];
}>();

const auth = useAuthStore();
const displayName = computed(() => auth.user?.nickname || auth.user?.username || '未登录用户');
const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase());
const categoryCount = computed(() => Math.max(0, props.categories.length - 1));
</script>

<template>
  <aside class="resource-sidebar">
    <section class="side-card welcome-card">
      <span class="welcome-avatar">{{ avatarText }}</span>
      <p class="welcome-kicker">欢迎访问</p>
      <h2>{{ displayName }}</h2>
      <router-link :to="auth.isAuthenticated ? '/user' : '/login'">
        {{ auth.isAuthenticated ? '查看个人中心' : '登录后同步收藏' }}
      </router-link>
    </section>

    <section class="side-card category-card">
      <header class="side-title">
        <span>主要领域</span>
        <small>{{ categoryCount }} 类</small>
      </header>
      <nav class="category-nav" aria-label="资源分类">
        <button
          v-for="item in categories"
          :key="item.value || 'all'"
          class="category-button"
          :data-category="item.value"
          :class="{ active: activeCategory === item.value }"
          type="button"
          @click="$emit('select', item.value)"
        >
          <span class="category-dot" aria-hidden="true"></span>
          {{ item.label }}
        </button>
      </nav>
    </section>

    <section class="side-card mini-card">
      <header class="side-title">
        <span>最近访问</span>
        <small>{{ auth.isAuthenticated ? '同步开启' : '登录可见' }}</small>
      </header>
      <p>{{ auth.isAuthenticated ? '查看最近浏览过的资源，继续上次的探索。' : '登录后记录最近 20 条访问，并在设备间同步。' }}</p>
      <router-link class="side-footer" :to="auth.isAuthenticated ? '/user/recent-visits' : '/login'">查看全部 <span aria-hidden="true">→</span></router-link>
    </section>

    <section class="side-card mini-card">
      <header class="side-title">
        <span>我的网站</span>
        <router-link class="add-site" :to="auth.isAuthenticated ? '/user/sites' : '/login'">添加</router-link>
      </header>
      <p>收藏常去的网站，统一放进自己的资源工作台。</p>
      <router-link class="side-footer" :to="auth.isAuthenticated ? '/user/sites' : '/login'">管理网站 <span aria-hidden="true">→</span></router-link>
    </section>
  </aside>
</template>

<style scoped>
.resource-sidebar { display: grid; gap: 15px; }
.side-card { padding: 18px; border: 1px solid #dfe6e1; border-radius: var(--radius); background: rgba(255, 255, 255, .94); box-shadow: var(--shadow-sm); }
.welcome-card { position: relative; overflow: hidden; color: #fff; background: #1e5a4e; }
.welcome-card::after { content: ""; position: absolute; right: -58px; bottom: -84px; width: 170px; height: 170px; border-radius: 50%; background: rgba(255, 255, 255, .07); }
.welcome-avatar { display: grid; width: 52px; height: 52px; place-items: center; border: 2px solid rgba(255, 255, 255, .55); border-radius: 50%; color: var(--brand-dark); background: #edf4f0; font-size: 20px; font-weight: 800; }
.welcome-kicker { margin: 13px 0 0; color: rgba(255, 255, 255, .72); font-size: 12px; }
.welcome-card h2 { position: relative; z-index: 1; margin: 5px 0 12px; font-size: 18px; }
.welcome-card a { position: relative; z-index: 1; display: inline-flex; align-items: center; min-height: 34px; padding: 0 12px; border: 1px solid rgba(255, 255, 255, .42); border-radius: 999px; color: #fff; font-size: 13px; text-decoration: none; }
.side-title { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 11px; font-size: 14px; font-weight: 800; }
.side-title small { color: var(--muted); font-size: 12px; font-weight: 500; }
.category-nav { display: grid; gap: 3px; }
.category-button { display: flex; align-items: center; gap: 10px; width: 100%; min-height: 40px; padding: 8px 10px; border: 0; border-radius: 11px; color: #4b5651; background: transparent; text-align: left; }
.category-button:hover { color: var(--brand); background: #f0f5f1; }
.category-button.active { color: var(--brand); background: #e7f1ed; box-shadow: inset 3px 0 0 var(--brand); font-weight: 700; }
.category-dot { width: 7px; height: 7px; border-radius: 50%; background: #c9d2cd; }
.category-button.active .category-dot { background: var(--brand); }
.mini-card p { margin: 0; color: var(--muted); font-size: 13px; line-height: 1.7; }
.side-footer { display: inline-flex; gap: 6px; margin-top: 10px; color: var(--brand); font-size: 13px; font-weight: 650; text-decoration: none; }
.add-site { color: var(--brand); font-size: 12px; text-decoration: none; }
@media (max-width: 820px) {
  .resource-sidebar { display: block; }
  .welcome-card, .mini-card { display: none; }
  .category-card { padding: 12px 14px; }
  .side-title { display: none; }
  .category-nav { display: flex; gap: 8px; overflow-x: auto; scrollbar-width: none; }
  .category-nav::-webkit-scrollbar { display: none; }
  .category-button { flex: 0 0 auto; width: auto; min-height: 36px; padding: 7px 12px; border-radius: 999px; white-space: nowrap; }
  .category-button.active { box-shadow: none; }
}
</style>