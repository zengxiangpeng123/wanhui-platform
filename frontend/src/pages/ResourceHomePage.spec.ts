import { createApp, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ElementPlus from 'element-plus';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fetchResources } from '../api/resources';
import HomePage from './HomePage.vue';
import LoginPage from './LoginPage.vue';
import RegisterPage from './RegisterPage.vue';
import ResourceHomePage from './ResourceHomePage.vue';

vi.mock('../api/auth', () => ({
  loadMe: vi.fn().mockResolvedValue(null),
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn()
}));
vi.mock('../api/resources', () => ({ fetchResources: vi.fn() }));

const designResource = {
  key: 'res-001',
  name: 'Figma',
  description: '协作设计工具',
  category: 'design',
  subcategory: 'design-tools',
  url: 'https://www.figma.com/',
  icon: '',
  tags: ['design', 'design-tools']
};

function pageWith(items: typeof designResource[]) {
  return {
    items,
    page: 1,
    size: 24,
    total: items.length,
    totalPages: items.length ? 1 : 0,
    sort: 'catalogOrder,asc'
  };
}

async function mountPage(path = '/') {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: ResourceHomePage },
      { path: '/login', component: LoginPage },
      { path: '/register', component: RegisterPage },
      { path: '/user', component: HomePage }
    ]
  });
  await router.push(path);
  await router.isReady();

  const root = document.createElement('div');
  document.body.appendChild(root);
  const app = createApp(ResourceHomePage);
  app.use(createPinia()).use(router).use(ElementPlus);
  app.mount(root);
  await nextTick();
  return { app, root, router };
}

describe('ResourceHomePage', () => {
  beforeEach(() => {
    vi.mocked(fetchResources).mockReset();
  });

  it('reloads resources when a category is selected', async () => {
    vi.mocked(fetchResources).mockImplementation(async (query) => pageWith(
      query.category === 'design' ? [designResource] : []
    ));
    const { app, root } = await mountPage('/');
    await vi.waitFor(() => expect(fetchResources).toHaveBeenCalled());

    const designButton = root.querySelector('[data-category="design"]') as HTMLButtonElement;
    expect(designButton).toBeTruthy();
    designButton.click();
    await vi.waitFor(() => {
      expect(fetchResources).toHaveBeenCalledWith(expect.objectContaining({ category: 'design' }));
      expect(root.textContent).toContain('Figma');
    });

    app.unmount();
    root.remove();
  });

  it('shows an empty state and clears the search query', async () => {
    vi.mocked(fetchResources).mockResolvedValue(pageWith([]));
    const { app, root, router } = await mountPage('/?q=missing');
    await vi.waitFor(() => expect(root.textContent).toContain('没有找到匹配资源'));

    (root.querySelector('[data-testid="clear-search"]') as HTMLButtonElement).click();
    await vi.waitFor(() => expect(router.currentRoute.value.fullPath).toBe('/'));

    app.unmount();
    root.remove();
  });
});
