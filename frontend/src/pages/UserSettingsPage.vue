<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import UserLayout from '../layouts/UserLayout.vue';
import { changePassword } from '../api/userProfile';
import { useAuthStore } from '../stores/auth';

const router = useRouter();
const auth = useAuthStore();

const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' });
const submitting = ref(false);
const errors = reactive<{ oldPassword?: string; newPassword?: string; confirmPassword?: string }>({});

function validate() {
  errors.oldPassword = form.oldPassword ? undefined : '请输入旧密码';
  if (!form.newPassword) errors.newPassword = '请输入新密码';
  else if (form.newPassword.length < 8 || form.newPassword.length > 64) errors.newPassword = '密码长度需为 8-64 位';
  else if (!/^(?=.*[A-Za-z])(?=.*\d).+$/.test(form.newPassword)) errors.newPassword = '密码需同时包含字母和数字';
  errors.confirmPassword = form.confirmPassword === form.newPassword ? undefined : '两次输入的新密码不一致';
  return !errors.oldPassword && !errors.newPassword && !errors.confirmPassword;
}

async function submit() {
  if (!validate()) return;
  submitting.value = true;
  try {
    await changePassword(form.oldPassword, form.newPassword);
    ElMessage.success('密码已修改，请重新登录');
    auth.clear();
    await router.replace('/login');
  } catch (requestError) {
    const typed = requestError as { message?: string; code?: string };
    if (typed.code === 'INVALID_OLD_PASSWORD') errors.oldPassword = '旧密码不正确';
    else ElMessage.error(typed.message || '密码修改失败');
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <UserLayout>
    <section class="settings-page">
      <header>
        <p class="eyebrow">SECURITY</p>
        <h1>账号安全</h1>
        <p class="lead">修改密码后当前会话将退出，需要使用新密码重新登录。</p>
      </header>

      <form class="settings-form" @submit.prevent="submit">
        <label class="field">
          <span class="field-label">当前密码</span>
          <input v-model="form.oldPassword" data-testid="old-password" type="password" autocomplete="current-password" />
          <span v-if="errors.oldPassword" class="field-error">{{ errors.oldPassword }}</span>
        </label>

        <label class="field">
          <span class="field-label">新密码</span>
          <input v-model="form.newPassword" data-testid="new-password" type="password" autocomplete="new-password" />
          <span v-if="errors.newPassword" class="field-error">{{ errors.newPassword }}</span>
        </label>

        <label class="field">
          <span class="field-label">确认新密码</span>
          <input v-model="form.confirmPassword" data-testid="confirm-password" type="password" autocomplete="new-password" />
          <span v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</span>
        </label>

        <div class="actions">
          <button type="submit" data-testid="password-submit" :disabled="submitting">
            {{ submitting ? '提交中…' : '修改密码' }}
          </button>
        </div>
      </form>
    </section>
  </UserLayout>
</template>

<style scoped>
.settings-page { max-width: 560px; margin: 0 auto; padding: 34px clamp(18px, 4vw, 48px) 72px; }
.eyebrow { margin: 0 0 8px; color: #4056ed; font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0; color: #172033; font-size: clamp(28px, 5vw, 42px); }
.lead { margin: 12px 0 26px; color: #667085; }
.settings-form { display: flex; flex-direction: column; gap: 18px; padding: 24px; border: 1px solid #edf0f8; border-radius: 14px; background: #fff; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field-label { color: #344054; font-size: 13px; font-weight: 600; }
.field input { padding: 10px 12px; border: 1px solid #d0d5dd; border-radius: 9px; font: inherit; outline: none; }
.field input:focus { border-color: #4056ed; box-shadow: 0 0 0 3px rgba(64, 86, 237, .12); }
.field-error { color: #d92d20; font-size: 12px; }
.actions { display: flex; justify-content: flex-end; }
.actions button { padding: 11px 20px; border: 0; border-radius: 9px; color: #fff; background: #4056ed; font: inherit; font-weight: 700; cursor: pointer; }
.actions button:disabled { opacity: .6; cursor: not-allowed; }
</style>
