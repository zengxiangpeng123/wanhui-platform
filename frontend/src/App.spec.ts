import { createApp, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ElementPlus from 'element-plus';
import { afterEach, describe, expect, it, vi } from 'vitest';
import App from './App.vue';
import HomePage from './pages/HomePage.vue';
import LoginPage from './pages/LoginPage.vue';

vi.mock('./api/auth', () => ({
  loadMe: vi.fn().mockResolvedValue(null),
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn()
}));

let cleanup: (() => void) | undefined;

afterEach(() => {
  cleanup?.();
  cleanup = undefined;
});

describe('App', () => {
  it('renders the application shell and home page', async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', component: HomePage },
        { path: '/login', component: LoginPage },
        { path: '/register', component: { template: '<div>register</div>' } }
      ]
    });
    await router.push('/');
    await router.isReady();

    const root = document.createElement('div');
    document.body.appendChild(root);
    const app = createApp(App);
    app.use(createPinia()).use(router).use(ElementPlus);
    app.mount(root);
    await nextTick();

    expect(root.textContent).toContain('万汇网');
    expect(root.textContent).toContain('发现值得收藏');
    cleanup = () => {
      app.unmount();
      root.remove();
    };
  });
});
