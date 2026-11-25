import { createApp, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ElementPlus, { ElMessageBox } from 'element-plus';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { deleteAdminPost, fetchAdminPosts } from '../api/admin';
import AdminContentPage from './AdminContentPage.vue';

vi.mock('../api/admin', () => ({
  fetchAdminPosts: vi.fn(),
  deleteAdminPost: vi.fn()
}));
vi.mock('../api/auth', () => ({
  loadMe: vi.fn().mockResolvedValue({
    id: '2',
    username: 'admin',
    nickname: '管理员',
    avatarKey: 'avatar_01',
    emailMasked: 'adm***@example.com',
    phoneMasked: null,
    roles: ['ADMIN'],
    permissions: ['POST_DELETE_ANY', 'ADMIN_CONTENT_VIEW']
  }),
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn()
}));
vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal<typeof import('element-plus')>();
  return { ...actual, ElMessageBox: { confirm: vi.fn().mockResolvedValue('confirm') } };
});

const postsPage = {
  items: [
    {
      id: '1',
      title: '待审核帖子',
      excerpt: '正文内容',
      author: { id: '1', username: 'member', nickname: '普通用户' },
      category: { code: 'academic', name: '学术资源' },
      tags: [],
      viewCount: 0,
      commentCount: 0,
      likeCount: 0,
      favoriteCount: 0,
      createdAt: '2026-11-24T10:00:00'
    }
  ],
  page: 1,
  size: 20,
  total: 1,
  totalPages: 1
};

async function mountPage() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div>home</div>' } },
      { path: '/admin/content', component: AdminContentPage }
    ]
  });
  await router.push('/admin/content');
  await router.isReady();
  const root = document.createElement('div');
  document.body.appendChild(root);
  const app = createApp(AdminContentPage);
  app.use(createPinia()).use(router).use(ElementPlus);
  app.mount(root);
  await nextTick();
  return { app, root };
}

describe('AdminContentPage', () => {
  beforeEach(() => {
    vi.mocked(fetchAdminPosts).mockReset();
    vi.mocked(deleteAdminPost).mockReset();
    vi.mocked(ElMessageBox.confirm).mockClear();
    vi.mocked(fetchAdminPosts).mockResolvedValue(postsPage);
    vi.mocked(deleteAdminPost).mockResolvedValue(undefined);
  });

  it('renders moderation list for admin user', async () => {
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(root.textContent).toContain('待审核帖子'));
    expect(root.textContent).toContain('内容管理');
    app.unmount();
    root.remove();
  });

  it('deletes post after confirmation and refreshes list', async () => {
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(root.textContent).toContain('待审核帖子'));

    (root.querySelector('[data-testid="delete-post-1"]') as HTMLButtonElement)
      .dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));

    await vi.waitFor(() => expect(ElMessageBox.confirm).toHaveBeenCalled());
    await vi.waitFor(() => expect(deleteAdminPost).toHaveBeenCalledWith('1'));
    await vi.waitFor(() => expect(fetchAdminPosts).toHaveBeenCalledTimes(2));
    app.unmount();
    root.remove();
  });
});
