<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import AppShell from '../layouts/AppShell.vue';

const auth = useAuthStore();
const router = useRouter();
const username = ref('');
const email = ref('');
const password = ref('');
const confirmPassword = ref('');
const errors = ref<{ username?: string; email?: string; password?: string; confirmPassword?: string }>({});
const submitting = ref(false);

function validate() {
  errors.value = {
    username: /^[A-Za-z0-9_-]{3,32}$/.test(username.value.trim())
      ? undefined
      : '用户名需为 3～32 位字母、数字、下划线或中划线',
    email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim()) ? undefined : '请输入有效邮箱',
    password: password.value.length >= 8 && /[A-Za-z]/.test(password.value) && /\d/.test(password.value)
      ? undefined
      : '密码至少 8 位，且同时包含字母和数字',
    confirmPassword: confirmPassword.value === password.value ? undefined : '两次输入的密码不一致'
  };
  return !errors.value.username && !errors.value.email && !errors.value.password && !errors.value.confirmPassword;
}

async function submit() {
  if (!validate() || submitting.value) return;
  submitting.value = true;
  try {
    await auth.register(username.value.trim(), email.value.trim(), password.value);
    await router.replace({ path: '/login', query: { registered: '1', username: username.value.trim() } });
  } catch {
    password.value = '';
    confirmPassword.value = '';
  } finally {
    submitting.value = false;
  }
}</script>

<template>
  <AppShell>
    <section class="auth-wrap">
      <aside class="auth-promo">
        <span class="promo-badge">加入万汇网</span>
        <h1>建立自己的资源收藏与讨论空间。</h1>
        <p>注册账号后，即可收藏工具、记录最近访问并参与社区交流。</p>
        <ul>
          <li><span>✓</span> 统一管理常用资源与自定义网站</li>
          <li><span>✓</span> 参与论坛讨论，分享使用经验</li>
          <li><span>✓</span> 账号数据由服务端会话保护</li>
        </ul>
      </aside>

      <div class="auth-card">
        <h2>创建账号</h2>
        <p class="card-lead">填写基本信息完成注册。</p>

        <el-alert v-if="auth.error" :title="auth.error" type="error" :closable="false" show-icon />

        <el-form class="auth-form" @submit.prevent="submit">
          <el-form-item label="用户名" :error="errors.username">
            <el-input v-model="username" autocomplete="username" placeholder="请输入用户名" data-testid="register-username" />
            <span class="field-hint">3～32 位字母、数字、_ 或 -</span>
            <p v-if="errors.username" class="field-error">{{ errors.username }}</p>
          </el-form-item>
          <el-form-item label="邮箱" :error="errors.email">
            <el-input v-model="email" type="email" autocomplete="email" placeholder="name@example.com" data-testid="register-email" />
            <p v-if="errors.email" class="field-error">{{ errors.email }}</p>
          </el-form-item>
          <div class="password-row">
            <el-form-item label="密码" :error="errors.password">
              <el-input v-model="password" type="password" autocomplete="new-password" show-password placeholder="至少 8 位，含字母和数字" data-testid="register-password" />
              <p v-if="errors.password" class="field-error">{{ errors.password }}</p>
            </el-form-item>
            <el-form-item label="确认密码" :error="errors.confirmPassword">
              <el-input v-model="confirmPassword" type="password" autocomplete="new-password" show-password placeholder="再次输入密码" data-testid="register-confirm-password" />
              <p v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</p>
            </el-form-item>
          </div>
          <button class="submit-button" type="button" :disabled="submitting" data-testid="register-submit" @click="submit">
            {{ submitting ? '注册中…' : '注册' }}
          </button>
        </el-form>

        <p class="switch-line">已有账号？<router-link to="/login">返回登录</router-link></p>
      </div>
    </section>
  </AppShell>
</template>

<style scoped>
.auth-wrap { display: grid; grid-template-columns: minmax(0, 1fr) 520px; align-items: center; gap: 64px; width: min(1120px, 100%); min-height: calc(100vh - 150px); margin: 0 auto; padding: 28px 12px; }
.auth-promo { max-width: 560px; }
.promo-badge { display: inline-flex; padding: 5px 10px; border-radius: 999px; color: var(--brand); background: #e7f1ed; font-size: 12px; font-weight: 700; }
.auth-promo h1 { margin: 16px 0 0; color: var(--ink); font-size: clamp(32px, 4vw, 44px); line-height: 1.2; letter-spacing: -.04em; }
.auth-promo > p { margin: 16px 0 24px; color: var(--muted); font-size: 15px; line-height: 1.8; }
.auth-promo ul { display: grid; gap: 12px; margin: 0; padding: 0; list-style: none; color: #4f5d57; }
.auth-promo li { display: flex; align-items: center; gap: 9px; font-size: 14px; }
.auth-promo li span { display: grid; width: 22px; height: 22px; place-items: center; border-radius: 50%; color: #fff; background: var(--brand); font-size: 12px; font-weight: 800; }
.auth-card { width: 100%; padding: 32px; border: 1px solid #dfe6e1; border-radius: 18px; background: #fff; box-shadow: var(--shadow); }
.auth-card h2 { margin: 0; color: var(--ink); font-size: 27px; }
.card-lead { margin: 7px 0 24px; color: var(--muted); font-size: 14px; }
.auth-form { margin-top: 8px; }
.password-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.field-hint { width: 100%; margin-top: 5px; color: #87928c; font-size: 11px; }
.field-error { width: 100%; margin: 4px 0 0; color: var(--danger); font-size: 12px; line-height: 1.5; }
.submit-button { width: 100%; min-height: 44px; margin-top: 4px; border: 0; border-radius: 9px; color: #fff; background: var(--brand); font-weight: 700; }
.submit-button:hover { background: var(--brand-dark); }
.submit-button:disabled { opacity: .65; cursor: not-allowed; }
.switch-line { margin: 18px 0 0; color: var(--muted); font-size: 13px; }
.switch-line a { color: var(--brand); font-weight: 650; }
:deep(.el-form-item__label) { color: #394740; font-size: 13px; font-weight: 650; }
:deep(.el-input__wrapper) { min-height: 42px; border-radius: 9px; box-shadow: 0 0 0 1px #d5ded8 inset; }
:deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px #75a99a inset, 0 0 0 4px rgba(23, 107, 91, .09); }
@media (max-width: 920px) {
  .auth-wrap { grid-template-columns: 1fr; max-width: 640px; gap: 24px; min-height: 0; padding: 12px 0; }
  .auth-promo { display: none; }
}
@media (max-width: 560px) {
  .auth-card { padding: 24px; }
  .password-row { grid-template-columns: 1fr; gap: 0; }
}
</style>