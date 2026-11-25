import type { Pinia } from 'pinia';
import type { Router } from 'vue-router';
import { useAuthStore } from '../stores/auth';

export function installAuthGuard(router: Router, pinia: Pinia) {
  router.beforeEach(async (to) => {
    const auth = useAuthStore(pinia);
    if (!auth.initialized) {
      await auth.loadMe();
    }

    if (to.meta.requiresAuth && !auth.isAuthenticated) {
      return { name: 'login', query: { redirect: to.fullPath } };
    }

    if (to.meta.guestOnly && auth.isAuthenticated) {
      return { name: 'home' };
    }

    return true;
  });
}
