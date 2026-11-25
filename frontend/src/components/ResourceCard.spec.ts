import { createApp, nextTick } from 'vue';
import { describe, expect, it } from 'vitest';
import ResourceCard from './ResourceCard.vue';

describe('ResourceCard', () => {
  it('renders resource content and opens the visit link in a new tab', async () => {
    const root = document.createElement('div');
    document.body.appendChild(root);
    const app = createApp(ResourceCard, {
      resource: {
        key: 'res-001',
        name: 'Figma',
        description: '协作设计工具',
        category: 'design',
        subcategory: 'design-tools',
        url: 'https://www.figma.com/',
        icon: 'https://example.com/figma.png',
        tags: ['design', 'design-tools']
      }
    });
    app.mount(root);
    await nextTick();

    expect(root.textContent).toContain('Figma');
    expect(root.textContent).toContain('协作设计工具');
    expect(root.textContent).toContain('访问网站');
    const image = root.querySelector('img');
    expect(image?.getAttribute('src')).toBe('https://example.com/figma.png');
    const link = root.querySelector('a');
    expect(link?.getAttribute('href')).toBe('https://www.figma.com/');
    expect(link?.getAttribute('target')).toBe('_blank');
    expect(link?.getAttribute('rel')).toContain('noopener');

    app.unmount();
    root.remove();
  });
});
