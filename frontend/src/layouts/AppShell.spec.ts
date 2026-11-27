import { createApp, defineComponent, h, nextTick } from 'vue';
import { createPinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import { afterEach, describe, expect, it, vi } from 'vitest';
import AppShell from './AppShell.vue';

vi.mock('../api/auth', () => ({
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

async function mountShell(withSidebar: boolean) {
  const blank = { template: '<div />' };
  const router = createRouter({
    history: createMemoryHistory(),
    routes: ['/', '/forum', '/login', '/register', '/about', '/privacy', '/terms']
      .map((path) => ({ path, component: blank }))
  });
  await router.push('/');
  await router.isReady();

  const Root = defineComponent({
    setup() {
      return () => h(
        AppShell,
        null,
        withSidebar
          ? { sidebar: () => h('aside', 'sidebar'), default: () => h('section', 'content') }
          : { default: () => h('section', 'content') }
      );
    }
  });

  const root = document.createElement('div');
  document.body.appendChild(root);
  const app = createApp(Root);
  app.use(createPinia()).use(router);
  app.mount(root);
  await nextTick();
  cleanup = () => {
    app.unmount();
    root.remove();
  };
  return root;
}

describe('AppShell', () => {
  it('uses a full-width body when no sidebar slot is provided', async () => {
    const root = await mountShell(false);
    const body = root.querySelector('.app-body');

    expect(body?.classList.contains('app-body--full')).toBe(true);
    expect(body?.classList.contains('app-body--with-sidebar')).toBe(false);
  });

  it('uses the two-column body when a sidebar slot is provided', async () => {
    const root = await mountShell(true);
    const body = root.querySelector('.app-body');

    expect(body?.classList.contains('app-body--with-sidebar')).toBe(true);
    expect(body?.classList.contains('app-body--full')).toBe(false);
  });
});