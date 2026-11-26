import { createApp, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import ElementPlus from 'element-plus';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { setCommentLike, type Comment } from '../api/forum';
import CommentList from './CommentList.vue';

vi.mock('../api/forum', () => ({
  setCommentLike: vi.fn()
}));

const comments: Comment[] = [
  {
    id: '10',
    content: '很有帮助',
    author: { id: '2', username: 'other', nickname: '其他用户' },
    likeCount: 2,
    liked: false,
    createdAt: '2026-11-24T11:00:00'
  }
];

async function mountList(onLikeError = vi.fn(), onUnauthorized = vi.fn()) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/', component: { template: '<div>home</div>' } }]
  });
  await router.push('/');
  await router.isReady();
  const root = document.createElement('div');
  document.body.appendChild(root);
  const app = createApp(CommentList, {
    comments,
    loading: false,
    error: null,
    onLikeError,
    onUnauthorized
  });
  app.use(createPinia()).use(router).use(ElementPlus);
  app.mount(root);
  await nextTick();
  return { app, root };
}

function buttonText(button: HTMLButtonElement) {
  return button.textContent?.replace(/\s+/g, ' ').trim();
}

describe('CommentList 点赞', () => {
  beforeEach(() => {
    vi.mocked(setCommentLike).mockReset();
    comments[0].liked = false;
    comments[0].likeCount = 2;
  });

  it('点击后展示选中态并更新计数', async () => {
    vi.mocked(setCommentLike).mockResolvedValue({ liked: true, likeCount: 3 });
    const { app, root } = await mountList();
    const button = root.querySelector('[data-testid="comment-like-10"]') as HTMLButtonElement;
    expect(buttonText(button)).toBe('赞 2');

    button.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
    await vi.waitFor(() => expect(setCommentLike).toHaveBeenCalledWith('10', true));
    await vi.waitFor(() => {
      expect(buttonText(button)).toBe('已赞 3');
      expect(button.getAttribute('aria-pressed')).toBe('true');
    });

    app.unmount();
    root.remove();
  });

  it('点赞失败时回滚选中态和计数', async () => {
    vi.mocked(setCommentLike).mockRejectedValue(Object.assign(new Error('网络异常'), { status: 500 }));
    const onLikeError = vi.fn();
    const { app, root } = await mountList(onLikeError);
    const button = root.querySelector('[data-testid="comment-like-10"]') as HTMLButtonElement;

    button.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
    await vi.waitFor(() => expect(onLikeError).toHaveBeenCalledWith('网络异常'));
    await vi.waitFor(() => {
      expect(buttonText(button)).toBe('赞 2');
      expect(button.getAttribute('aria-pressed')).toBe('false');
    });

    app.unmount();
    root.remove();
  });

  it('未登录点赞时触发登录引导', async () => {
    vi.mocked(setCommentLike).mockRejectedValue(Object.assign(new Error('请先登录'), { status: 401 }));
    const onUnauthorized = vi.fn();
    const { app, root } = await mountList(vi.fn(), onUnauthorized);

    (root.querySelector('[data-testid="comment-like-10"]') as HTMLButtonElement)
      .dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));

    await vi.waitFor(() => expect(onUnauthorized).toHaveBeenCalled());
    app.unmount();
    root.remove();
  });
});
