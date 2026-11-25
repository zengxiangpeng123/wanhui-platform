import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { loadMe } from '../api/auth';
import { ApiRequestError } from '../api/http';
import { useAuthStore } from './auth';

vi.mock('../api/auth', () => ({
  loadMe: vi.fn(),
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

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.mocked(loadMe).mockReset();
  });

  it('loadMe sets authenticated user', async () => {
    vi.mocked(loadMe).mockResolvedValue(user);
    const auth = useAuthStore();

    await auth.loadMe();

    expect(auth.isAuthenticated).toBe(true);
    expect(auth.user?.username).toBe('demo_user');
    expect(auth.initialized).toBe(true);
  });

  it('loadMe clears user state on 401', async () => {
    vi.mocked(loadMe).mockRejectedValue(new ApiRequestError('未登录', 401, 'AUTH_REQUIRED'));
    const auth = useAuthStore();
    auth.user = user;

    await auth.loadMe();

    expect(auth.isAuthenticated).toBe(false);
    expect(auth.user).toBeNull();
    expect(auth.initialized).toBe(true);
  });
});
