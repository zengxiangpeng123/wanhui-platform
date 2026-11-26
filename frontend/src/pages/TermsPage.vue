<script setup lang="ts">
import AppShell from '../layouts/AppShell.vue';
</script>

<template>
  <AppShell>
    <article class="site-page">
      <p class="eyebrow">TERMS</p>
      <h1>服务条款</h1>
      <p class="updated">更新日期：2026-11-26</p>
      <p class="lead">使用万汇网即表示你同意本条款与隐私政策。</p>

      <section>
        <h2>账号责任</h2>
        <ul>
          <li>你需妥善保管账号与密码，因个人保管不当造成的损失由你承担。</li>
          <li>不得冒用他人身份注册或使用服务。</li>
        </ul>
      </section>

      <section>
        <h2>用户行为</h2>
        <p>禁止发布或传播以下内容：</p>
        <ul>
          <li>违法违规、侵权、暴力、仇恨、色情或诈骗信息。</li>
          <li>破解软件、盗版资源、恶意代码。</li>
          <li>垃圾广告、刷屏、恶意灌水。</li>
          <li>侵犯他人隐私或未经授权公开他人信息。</li>
        </ul>
      </section>

      <section>
        <h2>内容权利</h2>
        <ul>
          <li>你对自己发布的内容保留权利，并授权万汇网在站内展示与存储。</li>
          <li>
            发现侵权内容可通过
            <a href="mailto:report@wanhui.example">report@wanhui.example</a>
            举报，我们会核实并处理。
          </li>
        </ul>
      </section>

      <section>
        <h2>服务变更与终止</h2>
        <p>我们可能调整或终止部分功能，并提前公告。严重违规账号可能被限制或删除。</p>
      </section>

      <section>
        <h2>免责声明</h2>
        <p>资源链接指向第三方网站，其内容与服务由第三方负责，万汇网不对其可用性与安全性作保证。</p>
      </section>

      <section>
        <h2>条款更新</h2>
        <p>条款更新后在站内公告；继续使用即视为接受更新后的条款。</p>
      </section>

      <nav class="page-links" aria-label="站点信息">
        <router-link to="/about">关于我们</router-link>
        <router-link to="/privacy">隐私政策</router-link>
      </nav>
    </article>
  </AppShell>
</template>

<style scoped>
.site-page { max-width: 860px; margin: 0 auto; padding: 42px clamp(18px, 4vw, 48px) 72px; }
.eyebrow { margin: 0 0 10px; color: #4056ed; font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0; color: #172033; font-size: clamp(32px, 6vw, 48px); letter-spacing: -.03em; }
.updated { margin: 8px 0 0; color: #98a2b3; font-size: 13px; }
.lead { margin: 16px 0 32px; color: #475467; font-size: 17px; line-height: 1.8; }
section { margin-bottom: 28px; }
h2 { margin: 0 0 12px; color: #172033; font-size: 19px; }
p { color: #475467; line-height: 1.75; }
ul { margin: 0; padding-left: 20px; color: #475467; line-height: 1.9; }
a { color: #4056ed; }
.page-links { display: flex; gap: 14px; margin-top: 36px; padding-top: 20px; border-top: 1px solid #edf0f8; }
.page-links a { padding: 9px 14px; border-radius: 9px; background: #f1f3ff; font-weight: 700; text-decoration: none; }
</style>
