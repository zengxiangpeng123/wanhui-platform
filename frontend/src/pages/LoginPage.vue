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
const showDemoAccount = import.meta.env.DEV;

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
    <section class="auth-wrap">
      <aside class="auth-promo">
        <span class="promo-badge">你的资源工作台</span>
        <h1>把常用资源留在同一个地方。</h1>
        <p>登录后可以收藏资源、参与论坛、同步自定义网站，并在不同设备查看个人数据。</p>
        <ul>
          <li><span>✓</span> 202 个精选资源，按领域持续维护</li>
          <li><span>✓</span> 资源收藏和最近访问跨设备同步</li>
          <li><span>✓</span> 服务端会话保护个人数据</li>
        </ul>
      </aside>

      <div class="auth-card">
        <h2>欢迎回来</h2>
        <p class="card-lead">登录你的万汇网账号。</p>

        <el-alert v-if="auth.error" :title="auth.error" type="error" :closable="false" show-icon />

        <el-form class="auth-form" @submit.prevent="submit">
          <el-form-item label="账号或邮箱" :error="errors.account">
            <el-input v-model="account" autocomplete="username" placeholder="请输入账号或邮箱" data-testid="login-account" />
            <p v-if="errors.account" class="field-error">{{ errors.account }}</p>
          </el-form-item>
          <el-form-item label="密码" :error="errors.password">
            <el-input v-model="password" type="password" autocomplete="current-password" show-password placeholder="请输入密码" data-testid="login-password" />
            <p v-if="errors.password" class="field-error">{{ errors.password }}</p>
          </el-form-item>
          <button class="submit-button" type="button" :disabled="submitting" data-testid="login-submit" @click="submit">
            {{ submitting ? '登录中…' : '登录' }}
          </button>
        </el-form>

        <p class="switch-line">还没有账号？<router-link to="/register">注册账号</router-link></p>
        <p v-if="showDemoAccount" class="demo-note">本地开发账号：demo_user / Demo1234!</p>
      </div>
    </section>
  </AppShell>
</template>

<style scoped>
.auth-wrap { display: grid; grid-template-columns: minmax(0, 1fr) 440px; align-items: center; gap: 72px; width: min(1040px, 100%); min-height: calc(100vh - 150px); margin: 0 auto; padding: 28px 12px; }
.auth-promo { max-width: 560px; }
.promo-badge { display: inline-flex; padding: 5px 10px; border-radius: 999px; color: var(--brand); background: #e7f1ed; font-size: 12px; font-weight: 700; }
.auth-promo h1 { margin: 16px 0 0; color: var(--ink); font-size: clamp(32px, 4vw, 45px); line-height: 1.2; letter-spacing: -.04em; }
.auth-promo > p { max-width: 520px; margin: 16px 0 24px; color: var(--muted); font-size: 15px; line-height: 1.8; }
.auth-promo ul { display: grid; gap: 12px; margin: 0; padding: 0; list-style: none; color: #4f5d57; }
.auth-promo li { display: flex; align-items: center; gap: 9px; font-size: 14px; }
.auth-promo li span { display: grid; width: 22px; height: 22px; place-items: center; border-radius: 50%; color: #fff; background: var(--brand); font-size: 12px; font-weight: 800; }
.auth-card { width: 100%; padding: 32px; border: 1px solid #dfe6e1; border-radius: 18px; background: #fff; box-shadow: var(--shadow); }
.auth-card h2 { margin: 0; color: var(--ink); font-size: 27px; }
.card-lead { margin: 7px 0 24px; color: var(--muted); font-size: 14px; }
.auth-form { margin-top: 8px; }
.field-error { width: 100%; margin: 4px 0 0; color: var(--danger); font-size: 12px; line-height: 1.5; }
.submit-button { width: 100%; min-height: 44px; margin-top: 4px; border: 0; border-radius: 9px; color: #fff; background: var(--brand); font-weight: 700; }
.submit-button:hover { background: var(--brand-dark); }
.submit-button:disabled { opacity: .65; cursor: not-allowed; }
.switch-line { margin: 18px 0 0; color: var(--muted); font-size: 13px; }
.switch-line a { color: var(--brand); font-weight: 650; }
.demo-note { margin: 18px 0 0; color: #8a958f; font-size: 12px; }
:deep(.el-form-item__label) { color: #394740; font-size: 13px; font-weight: 650; }
:deep(.el-input__wrapper) { min-height: 42px; border-radius: 9px; box-shadow: 0 0 0 1px #d5ded8 inset; }
:deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px #75a99a inset, 0 0 0 4px rgba(23, 107, 91, .09); }
@media (max-width: 820px) {
  .auth-wrap { grid-template-columns: 1fr; gap: 24px; min-height: 0; padding: 12px 0; }
  .auth-promo { display: none; }
  .auth-card { padding: 24px; }
}
</style>