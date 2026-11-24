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
const errors = ref<{ username?: string; email?: string; password?: string }>({});
const submitting = ref(false);

function validate() {
  errors.value = {
    username: /^[A-Za-z0-9_-]{3,32}$/.test(username.value.trim())
      ? undefined
      : '用户名需为 3～32 位字母、数字、下划线或中划线',
    email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim()) ? undefined : '请输入有效邮箱',
    password: password.value.length >= 8 && /[A-Za-z]/.test(password.value) && /\d/.test(password.value)
      ? undefined
      : '密码至少 8 位，且同时包含字母和数字'
  };
  return !errors.value.username && !errors.value.email && !errors.value.password;
}

async function submit() {
  if (!validate() || submitting.value) return;
  submitting.value = true;
  try {
    await auth.register(username.value.trim(), email.value.trim(), password.value);
    await router.replace({ path: '/login', query: { registered: '1', username: username.value.trim() } });
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
      <h1>创建账号</h1>
      <p class="lead">加入万汇网，保存你常用的资源与讨论。</p>

      <el-alert
        v-if="auth.error"
        :title="auth.error"
        type="error"
        :closable="false"
        show-icon
      />

      <el-form class="auth-form" @submit.prevent="submit">
        <el-form-item label="用户名" :error="errors.username">
          <el-input v-model="username" autocomplete="username" placeholder="3～32 位字符" data-testid="register-username" />
          <p v-if="errors.username" class="field-error">{{ errors.username }}</p>
        </el-form-item>
        <el-form-item label="邮箱" :error="errors.email">
          <el-input v-model="email" type="email" autocomplete="email" placeholder="you@example.com" data-testid="register-email" />
          <p v-if="errors.email" class="field-error">{{ errors.email }}</p>
        </el-form-item>
        <el-form-item label="密码" :error="errors.password">
          <el-input v-model="password" type="password" autocomplete="new-password" show-password placeholder="至少 8 位，包含字母和数字" data-testid="register-password" />
          <p v-if="errors.password" class="field-error">{{ errors.password }}</p>
        </el-form-item>
        <button class="submit-button" type="button" :disabled="submitting" data-testid="register-submit" @click="submit">
          {{ submitting ? '注册中…' : '注册' }}
        </button>
      </el-form>

      <p class="switch-line">已有账号？<router-link to="/login">去登录</router-link></p>
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
