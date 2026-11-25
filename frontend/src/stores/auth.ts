import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { login as loginApi, loadMe as loadMeApi, logout as logoutApi, register as registerApi } from '../api/auth';
import type { CurrentUser, RegisterResult } from '../api/auth';
import { ApiRequestError } from '../api/http';

export const useAuthStore = defineStore('auth', () => {
  const user = ref<CurrentUser | null>(null);
  const initialized = ref(false);
  const loading = ref(false);
  const error = ref<string | null>(null);

  const isAuthenticated = computed(() => user.value !== null);

  function clear() {
    user.value = null;
    error.value = null;
  }

  async function loadMe() {
    loading.value = true;
    try {
      user.value = await loadMeApi();
    } catch (requestError) {
      if (requestError instanceof ApiRequestError && requestError.status === 401) {
        clear();
      } else {
        error.value = requestError instanceof Error ? requestError.message : '加载登录状态失败';
      }
    } finally {
      initialized.value = true;
      loading.value = false;
    }
  }

  async function login(account: string, password: string) {
    loading.value = true;
    error.value = null;
    try {
      user.value = await loginApi(account, password);
      initialized.value = true;
      return user.value;
    } catch (requestError) {
      error.value = requestError instanceof Error ? requestError.message : '登录失败';
      throw requestError;
    } finally {
      loading.value = false;
    }
  }

  async function register(username: string, email: string, password: string): Promise<RegisterResult> {
    loading.value = true;
    error.value = null;
    try {
      return await registerApi(username, email, password);
    } catch (requestError) {
      error.value = requestError instanceof Error ? requestError.message : '注册失败';
      throw requestError;
    } finally {
      loading.value = false;
    }
  }

  async function logout() {
    try {
      await logoutApi();
    } finally {
      clear();
      initialized.value = true;
    }
  }

  return { user, initialized, loading, error, isAuthenticated, loadMe, login, register, logout, clear };
});
