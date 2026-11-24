import { createApp, nextTick } from 'vue';
import ElementPlus from 'element-plus';
import { describe, expect, it } from 'vitest';
import App from './App.vue';

describe('App', () => {
  it('renders the project shell and environment status', async () => {
    const root = document.createElement('div');
    document.body.appendChild(root);
    const app = createApp(App);
    app.use(ElementPlus);
    app.mount(root);
    await nextTick();
    expect(root.textContent).toContain('万汇网');
    expect(root.textContent).toContain('开发环境已就绪');
    app.unmount();
    root.remove();
  });
});
