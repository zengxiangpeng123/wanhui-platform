<script setup lang="ts">
defineProps<{
  modelValue: boolean;
  title: string;
  message: string;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  confirm: [];
}>();

function close() {
  emit('update:modelValue', false);
}

function confirm() {
  emit('confirm');
  close();
}
</script>

<template>
  <div v-if="modelValue" class="dialog-backdrop" data-testid="confirm-dialog" @click.self="close">
    <section class="dialog-card" role="dialog" aria-modal="true">
      <h2>{{ title }}</h2>
      <p>{{ message }}</p>
      <div class="dialog-actions">
        <button type="button" class="secondary" data-testid="confirm-cancel" @click="close">取消</button>
        <button type="button" class="danger" data-testid="confirm-ok" @click="confirm">确认删除</button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.dialog-backdrop { position: fixed; inset: 0; z-index: 50; display: grid; place-items: center; padding: 16px; background: rgba(16, 24, 40, .4); }
.dialog-card { width: min(420px, 100%); padding: 24px; border-radius: 16px; background: #fff; box-shadow: 0 24px 70px rgba(16, 24, 40, .2); }
h2 { margin: 0; color: #172033; font-size: 20px; }
p { margin: 12px 0 24px; color: #667085; line-height: 1.6; }
.dialog-actions { display: flex; justify-content: flex-end; gap: 10px; }
.dialog-actions button { padding: 9px 14px; border: 0; border-radius: 8px; font: inherit; font-weight: 700; cursor: pointer; }
.secondary { color: #475467; background: #f2f4f7; }
.danger { color: #fff; background: #d92d20; }
</style>
