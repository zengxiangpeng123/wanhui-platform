import { createApp, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ElementPlus from 'element-plus';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fetchProfile, updateProfile } from '../api/userProfile';
import UserProfilePage from './UserProfilePage.vue';

vi.mock('../api/userProfile', () => ({
  fetchProfile: vi.fn(),
  updateProfile: vi.fn(),
  changePassword: vi.fn()
}));
vi.mock('../api/auth', () => ({
  loadMe: vi.fn().mockResolvedValue({
    id: '1',
    username: 'profile_user',
    nickname: '资料用户',
    avatarKey: 'avatar_01',
    emailMasked: 'pro***@example.com',
    phoneMasked: null,
    roles: ['USER'],
    permissions: []
  }),
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn()
}));

const profile = {
  id: '1',
  username: 'profile_user',
  nickname: '资料用户',
  avatarKey: 'avatar_01',
  emailMasked: 'pro***@example.com',
  phoneMasked: null,
  interest: '前端开发',
  bio: '个人简介',
  createdAt: '2026-11-01T10:00:00'
};

async function mountPage() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div>home</div>' } },
      { path: '/user/profile', component: UserProfilePage },
      { path: '/user', component: { template: '<div>user</div>' } }
    ]
  });
  await router.push('/user/profile');
  await router.isReady();
  const root = document.createElement('div');
  document.body.appendChild(root);
  const app = createApp(UserProfilePage);
  app.use(createPinia()).use(router).use(ElementPlus);
  app.mount(root);
  await nextTick();
  return { app, root };
}

describe('UserProfilePage', () => {
  beforeEach(() => {
    vi.mocked(fetchProfile).mockReset();
    vi.mocked(updateProfile).mockReset();
    vi.mocked(fetchProfile).mockResolvedValue(profile);
    vi.mocked(updateProfile).mockResolvedValue(profile);
  });

  it('shows masked contacts as readonly fields', async () => {
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect((root.querySelector('[data-testid="profile-email"]') as HTMLInputElement)?.value).toBe('pro***@example.com'));
    const email = root.querySelector('[data-testid="profile-email"]') as HTMLInputElement;
    const phone = root.querySelector('[data-testid="profile-phone"]') as HTMLInputElement;
    expect(email.readOnly).toBe(true);
    expect(phone.readOnly).toBe(true);
    app.unmount();
    root.remove();
  });

  it('validates nickname and bio length', async () => {
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(fetchProfile).toHaveBeenCalled());

    const nickname = root.querySelector('[data-testid="profile-nickname"]') as HTMLInputElement;
    nickname.value = '   ';
    nickname.dispatchEvent(new Event('input', { bubbles: true }));
    await nextTick();
    (root.querySelector('[data-testid="profile-save"]') as HTMLButtonElement)
      .dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
    await nextTick();
    expect(root.textContent).toContain('昵称不能为空');

    nickname.value = 'x'.repeat(31);
    nickname.dispatchEvent(new Event('input', { bubbles: true }));
    await nextTick();
    (root.querySelector('[data-testid="profile-save"]') as HTMLButtonElement)
      .dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
    await nextTick();
    expect(root.textContent).toContain('昵称不能超过 30 个字符');
    app.unmount();
    root.remove();
  });

  it('saves trimmed profile values', async () => {
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(fetchProfile).toHaveBeenCalled());

    const nickname = root.querySelector('[data-testid="profile-nickname"]') as HTMLInputElement;
    nickname.value = '  新昵称  ';
    nickname.dispatchEvent(new Event('input', { bubbles: true }));
    await nextTick();
    (root.querySelector('[data-testid="profile-save"]') as HTMLButtonElement)
      .dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));

    await vi.waitFor(() => expect(updateProfile).toHaveBeenCalledWith({
      nickname: '新昵称',
      bio: '个人简介',
      interest: '前端开发'
    }));
    app.unmount();
    root.remove();
  });
});

