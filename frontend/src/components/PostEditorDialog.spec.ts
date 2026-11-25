import { createApp, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ElementPlus from 'element-plus';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPost } from '../api/forum';
import PostEditorDialog from '../components/PostEditorDialog.vue';

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

async function mountDialog() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/forum', component: { template: '<div>forum</div>' } }]
  });
  await router.push('/forum');
  await router.isReady();
  const root = document.createElement('div');
  document.body.appendChild(root);
  const app = createApp(PostEditorDialog, {
    modelValue: true,
    categories,
    onCreated: vi.fn()
  });
  app.use(createPinia()).use(router).use(ElementPlus);
  app.mount(root);
  await nextTick();
  return { app, root };
}

function setInput(root: HTMLElement, selector: string, value: string) {
  const input = root.querySelector(selector) as HTMLInputElement;
  input.value = value;
  input.dispatchEvent(new Event('input', { bubbles: true }));
}

describe('PostEditorDialog', () => {
  beforeEach(() => {
    vi.mocked(createPost).mockReset();
  });

  it('shows validation errors for empty title and content', async () => {
    const { app, root } = await mountDialog();
    (root.querySelector('[data-testid="post-submit"]') as HTMLButtonElement)
      .dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
    await nextTick();
    expect(root.textContent).toContain('请输入标题');
    expect(root.textContent).toContain('请输入正文');
    app.unmount();
    root.remove();
  });

  it('submits normalized payload and emits created post', async () => {
    vi.mocked(createPost).mockResolvedValue({
      id: '9',
      title: '新帖子',
      content: '正文内容',
      author: { id: '1', username: 'demo', nickname: '测试用户' },
      category: { code: 'academic', name: '学术资源' },
      tags: ['测试'],
      viewCount: 0,
      commentCount: 0,
      likeCount: 0,
      favoriteCount: 0,
      liked: false,
      favorited: false,
      createdAt: '2026-11-24T12:00:00'
    });
    const { app, root } = await mountDialog();
    setInput(root, '[data-testid="post-title"]', '  新帖子  ');
    setInput(root, '[data-testid="post-content"]', '正文内容');
    setInput(root, '[data-testid="post-tags"]', '测试,  发布');
    await nextTick();
    (root.querySelector('[data-testid="post-submit"]') as HTMLButtonElement)
      .dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));

    await vi.waitFor(() =>
      expect(createPost).toHaveBeenCalledWith({
        title: '新帖子',
        categoryCode: 'academic',
        tags: ['测试', '发布'],
        content: '正文内容'
      })
    );
    app.unmount();
    root.remove();
  });
});
