import { createApp, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ElementPlus from 'element-plus';
import { describe, expect, it, vi } from 'vitest';
import AboutPage from './AboutPage.vue';
import PrivacyPage from './PrivacyPage.vue';
import TermsPage from './TermsPage.vue';

vi.mock('../api/auth', () => ({
  loadMe: vi.fn().mockResolvedValue(null),
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn()
}));

async function mountAt(path: string, component: unknown) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div>home</div>' } },
      { path: '/about', component: AboutPage },
      { path: '/privacy', component: PrivacyPage },
      { path: '/terms', component: TermsPage }
    ]
  });
  await router.push(path);
  await router.isReady();
  const root = document.createElement('div');
  document.body.appendChild(root);
  const app = createApp(component as never);
  app.use(createPinia()).use(router).use(ElementPlus);
  app.mount(root);
  await nextTick();
  return { app, root };
}

describe('站点信息页面', () => {
  it('关于页面可访问并说明站点定位', async () => {
    const { app, root } = await mountAt('/about', AboutPage);
    expect(root.textContent).toContain('关于万汇网');
    expect(root.textContent).not.toContain('127.0.0.1');
    app.unmount();
    root.remove();
  });

  it('隐私页面包含数据收集、存储、删除渠道与 Cookie 说明', async () => {
    const { app, root } = await mountAt('/privacy', PrivacyPage);
    const text = root.textContent || '';
    expect(text).toContain('隐私政策');
    expect(text).toContain('我们收集的信息');
    expect(text).toContain('数据存储');
    expect(text).toContain('账号删除');
    expect(text).toContain('Cookie');
    expect(text).not.toContain('127.0.0.1');
    app.unmount();
    root.remove();
  });

  it('服务条款页面可访问并说明用户行为规范', async () => {
    const { app, root } = await mountAt('/terms', TermsPage);
    expect(root.textContent).toContain('服务条款');
    expect(root.textContent).toContain('用户行为');
    expect(root.textContent).not.toContain('127.0.0.1');
    app.unmount();
    root.remove();
  });
});
