<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();
const router = useRouter();

onMounted(() => {
  if (!auth.initialized) {
    auth.loadMe();
  }
});

async function logout() {
  await auth.logout();
  await router.replace('/');
}
</script>

<template>
  <header class="app-header">
    <router-link class="brand" to="/">万汇网</router-link>
    <nav class="app-nav" aria-label="主导航">
      <router-link to="/">资源</router-link>
      <router-link to="/forum">论坛</router-link>
      <router-link v-if="auth.isAuthenticated" to="/user">个人中心</router-link>
      <router-link v-if="auth.user?.roles.includes('ADMIN')" to="/admin/content">内容管理</router-link>
    </nav>
    <div class="account-actions">
      <template v-if="auth.isAuthenticated">
        <span class="account-name">{{ auth.user?.nickname || auth.user?.username }}</span>
        <button class="text-button" type="button" @click="logout">退出</button>
      </template>
      <template v-else>
        <router-link class="login-link" to="/login">登录</router-link>
        <router-link class="register-link" to="/register">注册</router-link>
      </template>
    </div>
  </header>
</template>

<style scoped>
.app-header { position: sticky; top: 0; z-index: 10; display: flex; align-items: center; gap: 28px; min-height: 76px; padding: 0 clamp(18px, 4vw, 48px); border-bottom: 1px solid rgba(224, 231, 255, .8); background: rgba(255, 255, 255, .92); backdrop-filter: blur(16px); }
.brand { color: #172033; font-size: 22px; font-weight: 800; text-decoration: none; letter-spacing: -.03em; }
.app-nav { display: flex; gap: 20px; }
.app-nav a, .login-link { color: #667085; text-decoration: none; }
.app-nav a.router-link-active { color: #4056ed; font-weight: 700; }
.account-actions { display: flex; align-items: center; gap: 14px; margin-left: auto; }
.account-name { color: #344054; font-weight: 600; }
.text-button { padding: 0; border: 0; color: #667085; background: transparent; font: inherit; cursor: pointer; }
.register-link { padding: 8px 14px; border-radius: 8px; color: #fff; background: #4056ed; text-decoration: none; }
@media (max-width: 560px) { .app-header { gap: 12px; } .app-nav { display: none; } .account-name { display: none; } }
</style>


