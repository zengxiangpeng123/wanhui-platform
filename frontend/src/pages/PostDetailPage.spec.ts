import { createApp, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ElementPlus from 'element-plus';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createComment, fetchComments, fetchPostDetail, setPostFavorite, setPostLike } from '../api/forum';
import PostDetailPage from './PostDetailPage.vue';

vi.mock('../api/auth', () => ({
  loadMe: vi.fn().mockResolvedValue({
    id: '1',
    username: 'demo',
    nickname: '测试用户',
    avatarKey: 'avatar_01',
    emailMasked: 'dem***@example.com',
    phoneMasked: null,
    roles: ['USER'],
    permissions: ['POST_CREATE', 'COMMENT_CREATE']
  }),
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
  createComment: vi.fn(),
  setCommentLike: vi.fn()
}));

const detail = {
  id: '1',
  title: '推荐几个好用的文献管理工具',
  content: '这是一篇帖子正文。',
  author: { id: '1', username: 'demo', nickname: '测试用户' },
  category: { code: 'academic', name: '学术资源' },
  tags: ['文献管理'],
  viewCount: 0,
  commentCount: 1,
  likeCount: 2,
  favoriteCount: 3,
  liked: false,
  favorited: false,
  createdAt: '2026-11-24T10:00:00'
};

const comments = {
  items: [
    {
      id: '10',
      content: '很有帮助',
      author: { id: '2', username: 'other', nickname: '其他用户' },
      likeCount: 0,
      liked: false,
      createdAt: '2026-11-24T11:00:00'
    }
  ],
  page: 1,
  size: 20,
  total: 1,
  totalPages: 1
};

async function mountPage(path = '/forum/post/1') {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div>home</div>' } },
      { path: '/forum', component: { template: '<div>forum</div>' } },
      { path: '/forum/post/:id', component: PostDetailPage },
      { path: '/login', component: { template: '<div>login</div>' } }
    ]
  });
  await router.push(path);
  await router.isReady();
  const root = document.createElement('div');
  document.body.appendChild(root);
  const app = createApp(PostDetailPage);
  app.use(createPinia()).use(router).use(ElementPlus);
  app.mount(root);
  await nextTick();
  return { app, root, router };
}

describe('PostDetailPage', () => {
  beforeEach(() => {
    vi.mocked(fetchPostDetail).mockReset();
    vi.mocked(fetchComments).mockReset();
    vi.mocked(setPostLike).mockReset();
    vi.mocked(setPostFavorite).mockReset();
    vi.mocked(createComment).mockReset();
    vi.mocked(fetchPostDetail).mockResolvedValue(detail);
    vi.mocked(fetchComments).mockResolvedValue(comments);
  });

  it('renders post body, comments and interaction counts', async () => {
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(root.textContent).toContain('这是一篇帖子正文。'));
    expect(root.textContent).toContain('很有帮助');
    expect(root.textContent).toContain('2');
    expect(root.textContent).toContain('3');
    app.unmount();
    root.remove();
  });

  it('toggles like and favorite state', async () => {
    vi.mocked(setPostLike).mockResolvedValue({ liked: true, likeCount: 3 });
    vi.mocked(setPostFavorite).mockResolvedValue({ favorited: true, favoriteCount: 4 });
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(fetchPostDetail).toHaveBeenCalled());

    (root.querySelector('[data-testid="toggle-like"]') as HTMLButtonElement)
      .dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
    await vi.waitFor(() => expect(setPostLike).toHaveBeenCalledWith('1', true));

    (root.querySelector('[data-testid="toggle-favorite"]') as HTMLButtonElement)
      .dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
    await vi.waitFor(() => expect(setPostFavorite).toHaveBeenCalledWith('1', true));

    await vi.waitFor(() => expect(root.textContent).toContain('已点赞'));
    app.unmount();
    root.remove();
  });

  it('submits a comment and appends it to the list', async () => {
    vi.mocked(createComment).mockResolvedValue({
      id: '11',
      content: '补充一条评论',
      author: { id: '1', username: 'demo', nickname: '测试用户' },
      likeCount: 0,
      liked: false,
      createdAt: '2026-11-24T12:00:00'
    });
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(fetchComments).toHaveBeenCalled());

    const textarea = root.querySelector('[data-testid="comment-input"]') as HTMLTextAreaElement;
    textarea.value = '补充一条评论';
    textarea.dispatchEvent(new Event('input', { bubbles: true }));
    await nextTick();
    (root.querySelector('[data-testid="comment-submit"]') as HTMLButtonElement)
      .dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));

    await vi.waitFor(() => expect(createComment).toHaveBeenCalledWith('1', '补充一条评论'));
    await vi.waitFor(() => expect(root.textContent).toContain('补充一条评论'));
    app.unmount();
    root.remove();
  });

  it('shows deleted state for 410 response', async () => {
    vi.mocked(fetchPostDetail).mockRejectedValue(
      Object.assign(new Error('帖子已删除'), { status: 410, code: 'POST_DELETED' })
    );
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(root.textContent).toContain('帖子已删除'));
    app.unmount();
    root.remove();
  });

  it('shows not-found state for 404 response', async () => {
    vi.mocked(fetchPostDetail).mockRejectedValue(
      Object.assign(new Error('帖子不存在'), { status: 404, code: 'RESOURCE_NOT_FOUND' })
    );
    const { app, root } = await mountPage();
    await vi.waitFor(() => expect(root.textContent).toContain('帖子不存在'));
    app.unmount();
    root.remove();
  });
});


