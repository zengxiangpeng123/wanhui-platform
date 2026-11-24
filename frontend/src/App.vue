<script setup lang="ts">
const toolchain = [
  { label: '前端', value: 'Vue 3 + TypeScript + Vite' },
  { label: '后端', value: 'Spring Boot 4 + Java 21' },
  { label: '数据库', value: 'MySQL 8.0 Docker' }
];
</script>

<template>
  <main class="app-shell">
    <section class="card">
      <p class="eyebrow">WANHUI PLATFORM</p>
      <h1>万汇网</h1>
      <p class="subtitle">开发环境已就绪。后续业务页面将在此基础上逐步实现。</p>
      <dl class="toolchain">
        <div v-for="item in toolchain" :key="item.label">
          <dt>{{ item.label }}</dt>
          <dd>{{ item.value }}</dd>
        </div>
      </dl>
      <el-button type="primary">开始开发</el-button>
    </section>
  </main>
</template>

<style scoped>
.app-shell { min-height: 100vh; display: grid; place-items: center; padding: 32px; background: radial-gradient(circle at top left, rgba(64, 86, 237, 0.18), transparent 38%), linear-gradient(135deg, #f7f9ff 0%, #eef2fb 100%); }
.card { width: min(680px, 100%); padding: clamp(28px, 6vw, 56px); border: 1px solid #e0e7ff; border-radius: 24px; background: rgba(255, 255, 255, 0.94); box-shadow: 0 24px 80px rgba(42, 62, 185, 0.12); }
.eyebrow { margin: 0 0 10px; color: #4056ed; font-size: 12px; font-weight: 700; letter-spacing: 0.16em; }
h1 { margin: 0; color: #172033; font-size: clamp(42px, 10vw, 76px); line-height: 1; }
.subtitle { margin: 20px 0 28px; color: #667085; font-size: 17px; }
.toolchain { display: grid; gap: 10px; margin: 0 0 28px; }
.toolchain div { display: flex; justify-content: space-between; gap: 16px; padding: 12px 14px; border-radius: 12px; background: #f6f8ff; }
.toolchain dt { color: #667085; }
.toolchain dd { margin: 0; color: #172033; font-weight: 600; text-align: right; }
@media (max-width: 520px) { .toolchain div { align-items: flex-start; flex-direction: column; gap: 4px; } .toolchain dd { text-align: left; } }
</style>
