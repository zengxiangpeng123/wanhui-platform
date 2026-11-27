<script setup lang="ts">
import AppShell from '../layouts/AppShell.vue';
</script>

<template>
  <AppShell>
    <article class="site-page">
      <p class="eyebrow">PRIVACY</p>
      <h1>隐私政策</h1>
      <p class="updated">更新日期：2026-11-26</p>
      <p class="lead">
        我们重视你的隐私。本政策说明万汇网收集哪些信息、如何使用与存储，以及你如何行使删除与导出权利。
      </p>

      <section>
        <h2>我们收集的信息</h2>
        <ul>
          <li><strong>账号信息</strong>：用户名、加密后的邮箱（用于登录与找回）、不可逆的密码哈希。</li>
          <li><strong>使用数据</strong>：收藏、最近访问、自定义网站、帖子与评论内容。</li>
          <li><strong>技术数据</strong>：会话 Cookie、CSRF 令牌、请求追踪 ID，用于安全与排障。</li>
        </ul>
        <p>我们不会收集身份证号、银行卡号、精确位置等敏感信息。</p>
      </section>

      <section>
        <h2>信息的使用</h2>
        <ul>
          <li>提供登录、收藏、发帖、评论等核心功能。</li>
          <li>保障账号与站点安全，例如防 CSRF、防暴力破解。</li>
          <li>统计站点可用性，不用于出售数据或广告画像。</li>
        </ul>
      </section>

      <section>
        <h2>数据存储</h2>
        <ul>
          <li>数据存储于部署所在区域的 MySQL 数据库。</li>
          <li>邮箱与手机号使用 AES-GCM 加密，并以 HMAC 建立唯一索引；密码使用 BCrypt 哈希。</li>
          <li>会话数据保存在服务端，浏览器仅保存随机会话标识。</li>
          <li>保留期限：账号存续期间；账号删除后 30 天内清除关联内容。</li>
        </ul>
      </section>

      <section>
        <h2>Cookie 与本地存储</h2>
        <ul>
          <li><strong>WH_SESSION</strong>：HttpOnly 会话 Cookie，用于维持登录状态。</li>
          <li><strong>XSRF-TOKEN</strong>：防跨站请求伪造令牌。</li>
          <li>前端不写入广告或第三方追踪 Cookie。</li>
        </ul>
      </section>

      <section>
        <h2>你的权利</h2>
        <ul>
          <li><strong>查询与更正</strong>：在「个人资料」中查看与修改昵称、简介。</li>
          <li>
            <strong>账号删除</strong>：发送邮件至
            <a href="mailto:privacy@wanhui.example">privacy@wanhui.example</a>，标题注明「账号删除」，
            我们将在 15 个工作日内处理。
          </li>
          <li><strong>数据导出</strong>：可申请导出收藏、帖子与评论记录。</li>
        </ul>
      </section>

      <section>
        <h2>未成年人保护</h2>
        <p>未满 14 周岁的用户应在监护人同意下使用本站。</p>
      </section>

      <section>
        <h2>政策更新</h2>
        <p>政策重大变更会在站内公告，并更新本页日期。</p>
      </section>

      <nav class="page-links" aria-label="站点信息">
        <router-link to="/about">关于我们</router-link>
        <router-link to="/terms">服务条款</router-link>
      </nav>
    </article>
  </AppShell>
</template>

<style scoped>
.site-page { max-width: 900px; margin: 0 auto; padding: 32px; border: 1px solid #dfe6e1; border-radius: 16px; background: #fff; box-shadow: var(--shadow-sm); }
.eyebrow { margin: 0 0 10px; color: var(--brand); font-size: 12px; font-weight: 700; letter-spacing: .16em; }
h1 { margin: 0; color: var(--ink); font-size: clamp(32px, 6vw, 48px); letter-spacing: -.03em; }
.updated { margin: 8px 0 0; color: #87928c; font-size: 13px; }
.lead { margin: 16px 0 32px; color: #59655f; font-size: 17px; line-height: 1.8; }
section { margin-bottom: 28px; }
h2 { margin: 0 0 12px; color: var(--ink); font-size: 19px; }
p { color: #59655f; line-height: 1.75; }
ul { margin: 0; padding-left: 20px; color: #59655f; line-height: 1.9; }
a { color: var(--brand); }
.page-links { display: flex; gap: 14px; margin-top: 36px; padding-top: 20px; border-top: 1px solid var(--line); }
.page-links a { padding: 9px 14px; border-radius: 9px; background: #e7f1ed; font-weight: 700; text-decoration: none; }
</style>
