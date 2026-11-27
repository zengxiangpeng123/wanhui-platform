<script setup lang="ts">
import AppShell from '../layouts/AppShell.vue';

const features = [
  { title: '资源库', text: '按设计、AI、学术、媒体分类收录常用工具与资料，支持搜索与收藏。' },
  { title: '论坛', text: '发布帖子、评论、点赞与收藏，交流工具心得与问题求助。' },
  { title: '个人空间', text: '收藏资源、记录最近访问、管理自定义网站与个人资料。' }
];
</script>

<template>
  <AppShell>
    <article class="site-page">
      <p class="eyebrow">ABOUT</p>
      <h1>关于万汇网</h1>
      <p class="lead">
        万汇网是一个面向学生、研究者与创意工作者的资源导航与交流社区。我们把分散在各地的工具、资料与灵感整理成可检索的目录，并提供论坛让用户分享使用经验。
      </p>

      <section>
        <h2>我们提供</h2>
        <div class="feature-grid">
          <div v-for="feature in features" :key="feature.title" class="feature-card">
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.text }}</p>
          </div>
        </div>
      </section>

      <section>
        <h2>我们坚持</h2>
        <ul>
          <li>只收录公开可访问的合法资源，不提供侵权内容或破解工具。</li>
          <li>尊重用户隐私，最小化收集个人信息。</li>
          <li>对违规内容提供举报与处理渠道。</li>
        </ul>
      </section>

      <section>
        <h2>联系我们</h2>
        <p>一般咨询：<a href="mailto:contact@wanhui.example">contact@wanhui.example</a></p>
        <p>内容举报：<a href="mailto:report@wanhui.example">report@wanhui.example</a></p>
      </section>

      <nav class="page-links" aria-label="站点信息">
        <router-link to="/privacy">隐私政策</router-link>
        <router-link to="/terms">服务条款</router-link>
      </nav>
    </article>
  </AppShell>
</template>

<style scoped>
.site-page { max-width: 900px; margin: 0 auto; padding: 32px; border: 1px solid #dfe6e1; border-radius: 16px; background: #fff; box-shadow: var(--shadow-sm); }
.eyebrow { margin: 0 0 10px; color: var(--brand); font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0; color: var(--ink); font-size: clamp(32px, 6vw, 48px); letter-spacing: -.03em; }
.lead { margin: 16px 0 32px; color: #59655f; font-size: 17px; line-height: 1.8; }
section { margin-bottom: 30px; }
h2 { margin: 0 0 14px; color: var(--ink); font-size: 20px; }
h3 { margin: 0 0 8px; color: var(--ink); font-size: 16px; }
p { color: #59655f; line-height: 1.75; }
ul { margin: 0; padding-left: 20px; color: #59655f; line-height: 1.9; }
.feature-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 14px; }
.feature-card { padding: 16px; border: 1px solid var(--line); border-radius: 12px; background: #fff; }
.feature-card p { margin: 0; font-size: 14px; }
a { color: var(--brand); }
.page-links { display: flex; gap: 14px; margin-top: 36px; padding-top: 20px; border-top: 1px solid var(--line); }
.page-links a { padding: 9px 14px; border-radius: 9px; background: #e7f1ed; font-weight: 700; text-decoration: none; }
</style>
