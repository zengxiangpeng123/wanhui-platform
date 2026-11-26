import { createApp, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ElementPlus from 'element-plus';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fetchCategories, fetchPosts, createPost } from '../api/forum';
import ForumPage from './ForumPage.vue';

vi.mock('../api/auth', () => ({
  loadMe: vi.fn().mockResolvedValue(null),
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn()
}));
vi.mock('../api/forum', () => ({
  fetchCategories: vi.fn(),
  fetchPosts: vi.fn(),
  createPost: vi.fn(),
  fetchPostDetail: vi.fn(),
  setPostLike: vi.fn(),
  setPostFavorite: vi.fn(),
  fetchComments: vi.fn(),
  createComment: vi.fn()
}));

const categories = [
  { code: 'academic', name: '学术资源' },
  { code: 'ai', name: 'AI 工具' }
];

const posts = {
  items: [
    {
      id: '1',
      title: '推荐几个好用的文献管理工具',
      excerpt: '这是一篇帖子正文。',
      author: { id: '1', username: 'demo', nickname: '测试用户' },
      category: { code: 'academic', name: '学术资源' },
      tags: ['文献管理'],
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
      { path: '/forum', component: ForumPage },
      { path: '/forum/post/:id', component: { template: '<div>detail</div>' } },
      { path: '/login', component: { template: '<div>login</div>' } }
    ]
  });
  await router.push('/forum');
  await router.isReady();
  const root = document.createElement('div');
  document.body.appendChild(root);
  const app = createApp(ForumPage);
  app.use(createPinia()).use(router).use(ElementPlus);
  app.mount(root);
  await nextTick();
  return { app, root, router };
}

describe('ForumPage', () => {
  beforeEach(() => {
    vi.mocked(fetchCategories).mockReset();
    vi.mocked(fetchPosts).mockReset();
    vi.mocked(createPost).mockReset();
    vi.mocked(fetchCategories).mockResolvedValue(categories);
    vi.mocked(fetchPosts).mockResolvedValue(posts);
  });

  it('renders posts and category tiles', async () => {
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(root.textContent).toContain('推荐几个好用的文献管理工具'));
    await vi.waitFor(() => expect(root.textContent).toContain('学术资源'));
    await vi.waitFor(() => expect(root.textContent).toContain('AI 工具'));
    app.unmount();
    root.remove();
  });

  it('requests filtered posts when a category is selected', async () => {
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(fetchPosts).toHaveBeenCalled());
    await vi.waitFor(() => expect(root.querySelector('[data-testid="category-ai"]')).toBeTruthy());
    const tile = root.querySelector('[data-testid="category-ai"]') as HTMLButtonElement;
    expect(tile).toBeTruthy();
    tile.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
    await vi.waitFor(() =>
      expect(vi.mocked(fetchPosts)).toHaveBeenLastCalledWith('ai', 1, 20)
    );
    app.unmount();
    root.remove();
  });

  it('shows an empty state when there are no posts', async () => {
    vi.mocked(fetchPosts).mockResolvedValue({ items: [], page: 1, size: 20, total: 0, totalPages: 0 });
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(root.textContent).toContain('还没有帖子'));
    app.unmount();
    root.remove();
  });

  it('shows retry action when loading fails', async () => {
    vi.mocked(fetchPosts).mockRejectedValue(new Error('网络异常'));
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(root.textContent).toContain('网络异常'));
    const retry = root.querySelector('[data-testid="forum-retry"]') as HTMLButtonElement;
    expect(retry).toBeTruthy();
    app.unmount();
    root.remove();
  });

  it('renders pagination when there are multiple pages', async () => {
    vi.mocked(fetchPosts).mockResolvedValue({ ...posts, total: 40, totalPages: 2 });
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(root.textContent).toContain('第 1 / 2 页'));
    app.unmount();
    root.remove();
  });
});

