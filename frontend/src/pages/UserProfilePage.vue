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

const avatarChoices = ['avatar_01', 'avatar_02', 'avatar_03', 'avatar_04', 'avatar_05', 'avatar_06'];
const form = reactive({ nickname: '', bio: '', interest: '', avatarKey: 'avatar_01' });

async function load() {
  loading.value = true;
  error.value = null;
  try {
    profile.value = await fetchProfile();
    form.nickname = profile.value.nickname;
    form.bio = profile.value.bio || '';
    form.interest = profile.value.interest || '';
    form.avatarKey = profile.value.avatarKey || 'avatar_01';
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
      interest: form.interest,
      avatarKey: form.avatarKey
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
        <div class="profile-summary">
          <span class="profile-avatar">{{ (profile.nickname || profile.username).slice(0, 1) }}</span>
          <div>
            <h2>{{ profile.nickname || profile.username }}</h2>
            <p>加入于 {{ new Date(profile.createdAt).toLocaleDateString('zh-CN') }} · 普通用户</p>
          </div>
        </div>
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

        <section class="avatar-section">
          <h3>选择预设头像</h3>
          <div class="avatar-grid">
            <button
              v-for="(avatar, index) in avatarChoices"
              :key="avatar"
              type="button"
              class="avatar-choice"
              :class="{ active: form.avatarKey === avatar }"
              :aria-label="`选择头像 ${index + 1}`"
              :data-testid="`profile-avatar-${index + 1}`"
              @click="form.avatarKey = avatar"
            >{{ index + 1 }}</button>
          </div>
        </section>

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
.profile-page { min-width: 0; }
header { margin-bottom: 18px; }
.eyebrow { display: inline-flex; margin-bottom: 9px; padding: 5px 10px; border-radius: 999px; color: var(--brand); background: #e7f1ed; font-size: 12px; font-weight: 700; }
h1 { margin: 0; color: var(--ink); font-size: clamp(26px, 3vw, 32px); letter-spacing: -.025em; }
.lead { margin: 9px 0 0; color: var(--muted); font-size: 14px; }
.state-panel { display: flex; align-items: center; justify-content: center; min-height: 200px; border: 1px dashed #cbd6d0; border-radius: 16px; color: var(--muted); background: rgba(255, 255, 255, .72); }
.profile-form { display: flex; flex-direction: column; gap: 18px; padding: 28px; border: 1px solid #dfe6e1; border-radius: 16px; background: #fff; box-shadow: var(--shadow-sm); }
.profile-summary { display: flex; align-items: center; gap: 14px; padding-bottom: 20px; border-bottom: 1px solid #e4ebe7; }
.profile-avatar { display: grid; width: 64px; height: 64px; place-items: center; border-radius: 16px; color: #173f38; background: #e7f1ed; font-size: 24px; font-weight: 800; }
.profile-summary h2 { margin: 0; color: var(--ink); font-size: 20px; }
.profile-summary p { margin: 5px 0 0; color: var(--muted); font-size: 13px; }
.avatar-section h3 { margin: 0 0 10px; color: #3d4b45; font-size: 13px; }
.avatar-grid { display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 10px; }
.avatar-choice { min-height: 82px; border: 1px solid #d9e3dd; border-radius: 14px; color: #fff; background: linear-gradient(145deg, #4b9b88, #176b5b); font-size: 28px; font-weight: 800; }
.avatar-choice:hover { transform: translateY(-1px); }
.avatar-choice.active { border-color: var(--brand); box-shadow: 0 0 0 3px rgba(23, 107, 91, .12); }
.readonly-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field-label { color: #3d4b45; font-size: 13px; font-weight: 650; }
.field input, .field textarea { width: 100%; padding: 10px 12px; border: 1px solid #d5ded8; border-radius: 9px; color: #33413c; background: #fff; outline: none; }
.field input[readonly] { color: #6e7974; background: #f2f4f2; }
.field input:focus, .field textarea:focus { border-color: #75a99a; box-shadow: 0 0 0 4px rgba(23, 107, 91, .09); }
.field textarea { min-height: 128px; resize: vertical; }
.field-error { color: var(--danger); font-size: 12px; }
.actions { display: flex; justify-content: flex-end; }
.actions button { padding: 10px 18px; border: 0; border-radius: 9px; color: #fff; background: var(--brand); font-weight: 700; }
.actions button:hover { background: var(--brand-dark); }
.actions button:disabled { opacity: .6; cursor: not-allowed; }
@media (max-width: 760px) {
  .readonly-grid { grid-template-columns: 1fr; }
  .avatar-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .profile-form { padding: 20px; }
}
</style>