<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { addSite, deleteSite, fetchSites, type UserSite } from '../api/userResources';
import { ApiRequestError } from '../api/http';
import AppShell from '../layouts/AppShell.vue';
import EmptyState from '../components/EmptyState.vue';
import ConfirmDialog from '../components/ConfirmDialog.vue';

const sites = ref<UserSite[]>([]);
const loading = ref(true);
const saving = ref(false);
const error = ref<string | null>(null);
const name = ref('');
const url = ref('');
const iconUrl = ref('');
const errors = ref<{ name?: string; url?: string }>({});
const pendingDelete = ref<UserSite | null>(null);
const confirmOpen = ref(false);

async function load() {
  loading.value = true;
  try {
    sites.value = await fetchSites();
  } catch (requestError) {
    error.value = requestError instanceof Error ? requestError.message : '网站加载失败';
  } finally {
    loading.value = false;
  }
}

function validate() {
  errors.value = {
    name: name.value.trim() ? undefined : '请输入网站名称',
    url: /^https?:\/\/[^\s]+$/i.test(url.value.trim()) ? undefined : '请输入合法的 http/https 地址'
  };
  return !errors.value.name && !errors.value.url;
}

async function submit() {
  if (!validate() || saving.value) return;
  saving.value = true;
  error.value = null;
  try {
    const site = await addSite({
      name: name.value.trim(),
      url: url.value.trim(),
      iconUrl: iconUrl.value.trim() || undefined
    });
    sites.value = [site, ...sites.value];
    name.value = '';
    url.value = '';
    iconUrl.value = '';
  } catch (requestError) {
    if (requestError instanceof ApiRequestError && requestError.status === 409) {
      errors.value = { url: requestError.message || '该网站已添加' };
    } else {
      error.value = requestError instanceof Error ? requestError.message : '网站保存失败';
    }
  } finally {
    saving.value = false;
  }
}

function askDelete(site: UserSite) {
  pendingDelete.value = site;
  confirmOpen.value = true;
}

async function confirmDelete() {
  if (!pendingDelete.value) return;
  const site = pendingDelete.value;
  await deleteSite(site.id);
  sites.value = sites.value.filter((item) => item.id !== site.id);
  pendingDelete.value = null;
}

onMounted(load);
</script>

<template>
  <AppShell>
    <section class="user-page">
      <router-link class="back-link" to="/user">返回个人中心</router-link>
      <p class="eyebrow">MY SITES</p>
      <h1>我的网站</h1>

      <form class="site-form" data-testid="site-form" @submit.prevent="submit">
        <h2>添加常用网站</h2>
        <div class="form-grid">
          <label>
            <span>名称</span>
            <input v-model="name" data-testid="site-name" maxlength="100" placeholder="例如 Figma" />
            <small v-if="errors.name" data-testid="site-name-error">{{ errors.name }}</small>
          </label>
          <label>
            <span>网址</span>
            <input v-model="url" data-testid="site-url" maxlength="500" placeholder="https://example.com" />
            <small v-if="errors.url" data-testid="site-url-error">{{ errors.url }}</small>
          </label>
          <label>
            <span>图标地址（可选）</span>
            <input v-model="iconUrl" data-testid="site-icon" maxlength="500" placeholder="https://example.com/icon.png" />
          </label>
        </div>
        <button type="submit" data-testid="site-submit" :disabled="saving">{{ saving ? '保存中…' : '添加网站' }}</button>
      </form>

      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
      <div v-if="loading" class="loading">正在加载网站…</div>
      <EmptyState v-else-if="sites.length === 0" title="还没有自定义网站" description="添加常用网站后，可以在这里快速访问。" />
      <div v-else class="site-list">
        <article v-for="site in sites" :key="site.id" class="site-row">
          <div>
            <strong>{{ site.name }}</strong>
            <a :href="site.url" target="_blank" rel="noopener noreferrer">{{ site.url }}</a>
          </div>
          <button type="button" class="delete-button" :data-site-id="site.id" @click="askDelete(site)">删除</button>
        </article>
      </div>

      <ConfirmDialog
        v-model="confirmOpen"
        title="删除网站"
        :message="pendingDelete ? `确定删除“${pendingDelete.name}”吗？` : ''"
        @confirm="confirmDelete"
      />
    </section>
  </AppShell>
</template>

<style scoped>
.user-page { max-width: 920px; margin: 0 auto; padding: 42px clamp(18px, 4vw, 48px) 72px; }
.back-link { color: #667085; font-size: 14px; text-decoration: none; }
.eyebrow { margin: 26px 0 10px; color: #4056ed; font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0 0 26px; color: #172033; font-size: clamp(34px, 6vw, 54px); }
.site-form { margin-bottom: 28px; padding: 22px; border: 1px solid #e0e7ff; border-radius: 14px; background: #fbfcff; }
h2 { margin: 0 0 16px; color: #344054; font-size: 18px; }
.form-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
label { display: flex; flex-direction: column; gap: 6px; color: #475467; font-size: 13px; font-weight: 700; }
input { width: 100%; padding: 10px 12px; border: 1px solid #d0d5dd; border-radius: 8px; font: inherit; }
small { color: #d92d20; font-weight: 400; }
.site-form button { margin-top: 16px; padding: 10px 15px; border: 0; border-radius: 8px; color: #fff; background: #4056ed; font: inherit; font-weight: 700; cursor: pointer; }
.loading { color: #667085; }
.site-list { margin-top: 20px; border-top: 1px solid #edf0f8; }
.site-row { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 16px 0; border-bottom: 1px solid #edf0f8; }
.site-row div { display: flex; min-width: 0; flex-direction: column; gap: 5px; }
.site-row strong { color: #344054; }
.site-row a { overflow: hidden; color: #667085; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.delete-button { padding: 8px 12px; border: 1px solid #fecdca; border-radius: 8px; color: #d92d20; background: #fff; font: inherit; cursor: pointer; }
@media (max-width: 720px) { .form-grid { grid-template-columns: 1fr; } }
</style>
