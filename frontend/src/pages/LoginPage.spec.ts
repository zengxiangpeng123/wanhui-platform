import { createApp, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ElementPlus from 'element-plus';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { login } from '../api/auth';
import HomePage from './HomePage.vue';
import LoginPage from './LoginPage.vue';

vi.mock('../api/auth', () => ({
  loadMe: vi.fn().mockResolvedValue(null),
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn()
}));

const user = {
  id: '7',
  username: 'demo_user',
  nickname: '测试用户',
  avatarKey: 'avatar_01',
  emailMasked: 'dem***@example.com',
  phoneMasked: null,
  roles: ['USER'],
  permissions: ['POST_CREATE']
};

async function mountPage() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: HomePage },
      { path: '/login', component: LoginPage },
      { path: '/register', component: { template: '<div>register</div>' } },
      { path: '/user', component: { template: '<div>user</div>' } }
    ]
  });
  await router.push('/login?redirect=/user');
  await router.isReady();

  const pinia = createPinia();
  const root = document.createElement('div');
  document.body.appendChild(root);
  const app = createApp(LoginPage);
  app.use(pinia).use(router).use(ElementPlus);
  app.mount(root);
  await nextTick();
  return { app, root, router };
}

function setInput(root: HTMLElement, index: number, value: string) {
  const input = root.querySelectorAll('input')[index] as HTMLInputElement;
  input.value = value;
  input.dispatchEvent(new Event('input', { bubbles: true }));
}

function clickButton(root: HTMLElement) {
  const button = root.querySelector('[data-testid="login-submit"]') as HTMLButtonElement;
  button.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
}

describe('LoginPage', () => {
  beforeEach(() => {
    vi.mocked(login).mockReset();
  });

  it('shows field errors for empty input', async () => {
    const { app, root } = await mountPage();

    clickButton(root);
    await nextTick();

    expect(root.textContent).toContain('请输入账号');
    expect(root.textContent).toContain('请输入密码');
    app.unmount();
    root.remove();
  });

  it('replaces the route with redirect after successful login', async () => {
    vi.mocked(login).mockResolvedValue(user);
    const { app, root, router } = await mountPage();

    setInput(root, 0, 'demo_user');
    setInput(root, 1, 'Passw0rd!');
    clickButton(root);
    await nextTick();
    await Promise.resolve();
    await nextTick();

    expect(vi.mocked(login)).toHaveBeenCalledWith('demo_user', 'Passw0rd!');
    await vi.waitFor(() => expect(router.currentRoute.value.fullPath).toBe('/user'));
    app.unmount();
    root.remove();
  });
});
