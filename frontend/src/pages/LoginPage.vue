<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import AppShell from '../layouts/AppShell.vue';

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();
const account = ref('');
const password = ref('');
const errors = ref<{ account?: string; password?: string }>({});
const submitting = ref(false);

function redirectTarget() {
  const value = route.query.redirect;
  return typeof value === 'string' && value.startsWith('/') ? value : '/';
}

function validate() {
  errors.value = {
    account: account.value.trim() ? undefined : '请输入账号',
    password: password.value ? undefined : '请输入密码'
  };
  return !errors.value.account && !errors.value.password;
}

async function submit() {
  if (!validate() || submitting.value) return;
  submitting.value = true;
  try {
    await auth.login(account.value.trim(), password.value);
    await router.replace(redirectTarget());
  } catch {
    password.value = '';
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <AppShell>
  <section class="auth-shell">
    <div class="auth-card">
      <p class="eyebrow">WANHUI ACCOUNT</p>
      <h1>登录</h1>
      <p class="lead">登录后可以收藏资源、参与讨论和管理个人资料。</p>

      <el-alert
        v-if="auth.error"
        :title="auth.error"
        type="error"
        :closable="false"
        show-icon
      />

      <el-form class="auth-form" @submit.prevent="submit">
        <el-form-item label="账号" :error="errors.account">
          <el-input
            v-model="account"
            autocomplete="username"
            placeholder="用户名或邮箱"
            data-testid="login-account"
          />
          <p v-if="errors.account" class="field-error">{{ errors.account }}</p>
        </el-form-item>
        <el-form-item label="密码" :error="errors.password">
          <el-input
            v-model="password"
            type="password"
            autocomplete="current-password"
            show-password
            placeholder="请输入密码"
            data-testid="login-password"
          />
          <p v-if="errors.password" class="field-error">{{ errors.password }}</p>
        </el-form-item>
        <button class="submit-button" type="button" :disabled="submitting" data-testid="login-submit" @click="submit">
          {{ submitting ? '登录中…' : '登录' }}
        </button>
      </el-form>

      <p class="switch-line">还没有账号？<router-link to="/register">立即注册</router-link></p>
      <router-link class="back-link" to="/">返回首页</router-link>
    </div>
  </section>
  </AppShell>
</template>

<style scoped>
.auth-shell { min-height: calc(100vh - 76px); display: grid; place-items: center; padding: 32px 16px; }
.auth-card { width: min(460px, 100%); padding: 36px; border: 1px solid #e0e7ff; border-radius: 20px; background: #fff; box-shadow: 0 24px 70px rgba(42, 62, 185, .12); }
.eyebrow { margin: 0 0 10px; color: #4056ed; font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0; color: #172033; font-size: 42px; }
.lead { margin: 12px 0 24px; color: #667085; line-height: 1.7; }
.auth-form { margin-top: 22px; }
.field-error { width: 100%; margin: 4px 0 0; color: #d92d20; font-size: 12px; line-height: 1.5; }
.submit-button { width: 100%; margin-top: 4px; padding: 11px 16px; border: 0; border-radius: 8px; color: #fff; background: #4056ed; font: inherit; font-weight: 700; cursor: pointer; }
.switch-line { margin: 20px 0 12px; color: #667085; text-align: center; }
.switch-line a, .back-link { color: #4056ed; }
.back-link { display: block; text-align: center; font-size: 14px; text-decoration: none; }
</style>
