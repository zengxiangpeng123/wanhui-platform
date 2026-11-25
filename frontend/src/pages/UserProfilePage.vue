<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import UserLayout from '../layouts/UserLayout.vue';
import { fetchProfile, updateProfile, type UserProfile } from '../api/userProfile';

const profile = ref<UserProfile | null>(null);
const loading = ref(true);
const saving = ref(false);
const error = ref<string | null>(null);
const fieldErrors = reactive<{ nickname?: string; bio?: string }>({});

const form = reactive({ nickname: '', bio: '', interest: '' });

async function load() {
  loading.value = true;
  error.value = null;
  try {
    profile.value = await fetchProfile();
    form.nickname = profile.value.nickname;
    form.bio = profile.value.bio || '';
    form.interest = profile.value.interest || '';
  } catch (requestError) {
    error.value = requestError instanceof Error ? requestError.message : '个人资料加载失败';
  } finally {
    loading.value = false;
  }
}

function validate() {
  fieldErrors.nickname = undefined;
  fieldErrors.bio = undefined;
  if (!form.nickname.trim()) fieldErrors.nickname = '昵称不能为空';
  else if (form.nickname.trim().length > 30) fieldErrors.nickname = '昵称不能超过 30 个字符';
  if (form.bio.length > 500) fieldErrors.bio = '个人简介不能超过 500 个字符';
  return !fieldErrors.nickname && !fieldErrors.bio;
}

async function save() {
  if (!validate()) return;
  saving.value = true;
  try {
    profile.value = await updateProfile({
      nickname: form.nickname.trim(),
      bio: form.bio,
      interest: form.interest
    });
    ElMessage.success('个人资料已保存');
  } catch (requestError) {
    ElMessage.error(requestError instanceof Error ? requestError.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

onMounted(load);
</script>

<template>
  <UserLayout>
    <section class="profile-page">
      <header>
        <p class="eyebrow">PROFILE</p>
        <h1>个人资料</h1>
        <p class="lead">维护昵称、简介和兴趣方向，账号与联系方式不可在此修改。</p>
      </header>

      <div v-if="loading" class="state-panel">正在加载资料…</div>
      <el-alert v-else-if="error" :title="error" type="error" :closable="false" show-icon />
      <form v-else-if="profile" class="profile-form" @submit.prevent="save">
        <div class="readonly-grid">
          <label class="field">
            <span class="field-label">用户名</span>
            <input :value="profile.username" data-testid="profile-username" readonly />
          </label>
          <label class="field">
            <span class="field-label">邮箱（只读）</span>
            <input :value="profile.emailMasked" data-testid="profile-email" readonly />
          </label>
          <label class="field">
            <span class="field-label">手机号（只读）</span>
            <input :value="profile.phoneMasked || '未绑定'" data-testid="profile-phone" readonly />
          </label>
        </div>

        <label class="field">
          <span class="field-label">昵称</span>
          <input v-model="form.nickname" data-testid="profile-nickname" maxlength="30" />
          <span v-if="fieldErrors.nickname" class="field-error">{{ fieldErrors.nickname }}</span>
        </label>

        <label class="field">
          <span class="field-label">兴趣方向</span>
          <input v-model="form.interest" data-testid="profile-interest" maxlength="100" placeholder="例如：前端开发、科研工具" />
        </label>

        <label class="field">
          <span class="field-label">个人简介（{{ form.bio.length }} / 500）</span>
          <textarea v-model="form.bio" data-testid="profile-bio" rows="6" maxlength="500"></textarea>
          <span v-if="fieldErrors.bio" class="field-error">{{ fieldErrors.bio }}</span>
        </label>

        <div class="actions">
          <button type="submit" data-testid="profile-save" :disabled="saving">
            {{ saving ? '保存中…' : '保存修改' }}
          </button>
        </div>
      </form>
    </section>
  </UserLayout>
</template>

<style scoped>
.profile-page { max-width: 760px; margin: 0 auto; padding: 34px clamp(18px, 4vw, 48px) 72px; }
.eyebrow { margin: 0 0 8px; color: #4056ed; font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0; color: #172033; font-size: clamp(28px, 5vw, 42px); }
.lead { margin: 12px 0 26px; color: #667085; }
.state-panel { display: flex; align-items: center; justify-content: center; min-height: 200px; border: 1px dashed #d0d5dd; border-radius: 14px; color: #667085; background: #fbfcff; }
.profile-form { display: flex; flex-direction: column; gap: 18px; padding: 24px; border: 1px solid #edf0f8; border-radius: 14px; background: #fff; }
.readonly-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; padding-bottom: 6px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field-label { color: #344054; font-size: 13px; font-weight: 600; }
.field input, .field textarea { padding: 10px 12px; border: 1px solid #d0d5dd; border-radius: 9px; font: inherit; outline: none; }
.field input[readonly] { color: #667085; background: #f8f9fc; cursor: not-allowed; }
.field input:focus, .field textarea:focus { border-color: #4056ed; box-shadow: 0 0 0 3px rgba(64, 86, 237, .12); }
.field textarea { resize: vertical; line-height: 1.6; }
.field-error { color: #d92d20; font-size: 12px; }
.actions { display: flex; justify-content: flex-end; }
.actions button { padding: 11px 20px; border: 0; border-radius: 9px; color: #fff; background: #4056ed; font: inherit; font-weight: 700; cursor: pointer; }
.actions button:disabled { opacity: .6; cursor: not-allowed; }
</style>
