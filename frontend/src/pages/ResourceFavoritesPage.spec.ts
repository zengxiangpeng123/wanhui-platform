import { createApp, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ElementPlus from 'element-plus';
import { describe, expect, it, vi } from 'vitest';
import { fetchFavorites } from '../api/userResources';
import HomePage from './HomePage.vue';
import LoginPage from './LoginPage.vue';
import RegisterPage from './RegisterPage.vue';
import ResourceFavoritesPage from './ResourceFavoritesPage.vue';

vi.mock('../api/auth', () => ({
  loadMe: vi.fn().mockResolvedValue(null),
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn()
}));
vi.mock('../api/userResources', () => ({
  fetchFavorites: vi.fn(),
  fetchRecentVisits: vi.fn(),
  clearRecentVisits: vi.fn(),
  fetchSites: vi.fn(),
  addSite: vi.fn(),
  deleteSite: vi.fn()
}));

async function mountPage() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: HomePage },
      { path: '/login', component: LoginPage },
      { path: '/register', component: RegisterPage },
      { path: '/user', component: HomePage },
      { path: '/user/favorites', component: ResourceFavoritesPage }
    ]
  });
  await router.push('/user/favorites');
  await router.isReady();
  const root = document.createElement('div');
  document.body.appendChild(root);
  const app = createApp(ResourceFavoritesPage);
  app.use(createPinia()).use(router).use(ElementPlus);
  app.mount(root);
  await nextTick();
  return { app, root };
}

describe('ResourceFavoritesPage', () => {
  it('shows an empty state when there are no favorites', async () => {
    vi.mocked(fetchFavorites).mockResolvedValue({ items: [], page: 1, size: 20, total: 0, totalPages: 0 });
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(root.textContent).toContain('还没有收藏资源'));
    expect(root.textContent).toContain('去发现资源');
    app.unmount();
    root.remove();
  });
});
