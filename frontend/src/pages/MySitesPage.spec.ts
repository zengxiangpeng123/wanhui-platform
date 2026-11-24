import { createApp, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ElementPlus from 'element-plus';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiRequestError } from '../api/http';
import { addSite, deleteSite, fetchSites } from '../api/userResources';
import HomePage from './HomePage.vue';
import LoginPage from './LoginPage.vue';
import MySitesPage from './MySitesPage.vue';
import RegisterPage from './RegisterPage.vue';

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

const site = {
  id: '4',
  name: 'Figma',
  url: 'https://www.figma.com',
  iconUrl: null,
  createdAt: '2026-11-25T00:00:00'
};

async function mountPage() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: HomePage },
      { path: '/login', component: LoginPage },
      { path: '/register', component: RegisterPage },
      { path: '/user', component: HomePage },
      { path: '/user/sites', component: MySitesPage }
    ]
  });
  await router.push('/user/sites');
  await router.isReady();
  const root = document.createElement('div');
  document.body.appendChild(root);
  const app = createApp(MySitesPage);
  app.use(createPinia()).use(router).use(ElementPlus);
  app.mount(root);
  await nextTick();
  return { app, root };
}

function setInput(root: HTMLElement, testId: string, value: string) {
  const input = root.querySelector(`[data-testid="${testId}"]`) as HTMLInputElement;
  input.value = value;
  input.dispatchEvent(new Event('input', { bubbles: true }));
}

describe('MySitesPage', () => {
  beforeEach(() => {
    vi.mocked(fetchSites).mockReset();
    vi.mocked(addSite).mockReset();
    vi.mocked(deleteSite).mockReset();
  });

  it('asks for confirmation before deleting a site', async () => {
    vi.mocked(fetchSites).mockResolvedValue([site]);
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(root.textContent).toContain('Figma'));

    (root.querySelector('[data-site-id="4"]') as HTMLButtonElement).click();
    await nextTick();
    expect(root.querySelector('[data-testid="confirm-dialog"]')).not.toBeNull();

    (root.querySelector('[data-testid="confirm-ok"]') as HTMLButtonElement).click();
    await vi.waitFor(() => expect(deleteSite).toHaveBeenCalledWith('4'));
    await vi.waitFor(() => expect(root.textContent).not.toContain('Figma'));
    app.unmount();
    root.remove();
  });

  it('shows a field error when the URL is duplicated', async () => {
    vi.mocked(fetchSites).mockResolvedValue([]);
    vi.mocked(addSite).mockRejectedValue(new ApiRequestError('该网站已添加', 409, 'CONFLICT'));
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(root.textContent).toContain('还没有自定义网站'));

    setInput(root, 'site-name', 'Figma duplicate');
    setInput(root, 'site-url', 'https://www.figma.com/');
    (root.querySelector('[data-testid="site-form"]') as HTMLFormElement).dispatchEvent(
      new Event('submit', { bubbles: true, cancelable: true })
    );
    await vi.waitFor(() => expect(root.querySelector('[data-testid="site-url-error"]')?.textContent).toContain('该网站已添加'));
    app.unmount();
    root.remove();
  });
});
