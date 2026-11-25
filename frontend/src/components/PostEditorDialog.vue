<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { createPost, type PostCategory, type PostDetail } from '../api/forum';

const props = defineProps<{
  modelValue: boolean;
  categories: PostCategory[];
  defaultCategory?: string;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  created: [post: PostDetail];
  unauthorized: [];
}>();

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
});

const title = ref('');
const content = ref('');
const categoryCode = ref('');
const tagsInput = ref('');
const submitting = ref(false);
const error = ref<string | null>(null);
const fieldErrors = ref<{ title?: string; content?: string; category?: string }>({});

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      title.value = '';
      content.value = '';
      tagsInput.value = '';
      error.value = null;
      fieldErrors.value = {};
      categoryCode.value = props.defaultCategory || props.categories[0]?.code || '';
    }
  },
  { immediate: true }
);

function parseTags() {
  return tagsInput.value
    .split(/[,，\s]+/)
    .map((item) => item.trim())
    .filter(Boolean);
}

function validate() {
  const errors: typeof fieldErrors.value = {};
  if (!title.value.trim()) errors.title = '请输入标题';
  else if (title.value.trim().length > 100) errors.title = '标题不能超过 100 字';
  if (!content.value.trim()) errors.content = '请输入正文';
  else if (content.value.length > 20000) errors.content = '正文不能超过 20000 字';
  if (!categoryCode.value) errors.category = '请选择分类';
  fieldErrors.value = errors;
  return Object.keys(errors).length === 0;
}

async function submit() {
  error.value = null;
  if (!validate()) return;
  submitting.value = true;
  try {
    const post = await createPost({
      title: title.value.trim(),
      categoryCode: categoryCode.value,
      tags: parseTags().slice(0, 5),
      content: content.value
    });
    emit('created', post);
    visible.value = false;
  } catch (requestError) {
    const typed = requestError as { status?: number | null; message?: string };
    if (typed.status === 401) {
      emit('unauthorized');
    } else {
      error.value = typed.message || '发帖失败，请稍后重试';
    }
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <el-dialog v-model="visible" title="发布新帖" width="620px" :close-on-click-modal="false" :append-to-body="false">
    <div class="editor-form">
      <label class="field">
        <span class="field-label">标题</span>
        <input v-model="title" data-testid="post-title" maxlength="100" placeholder="一句话概括你的问题或分享" />
        <span v-if="fieldErrors.title" class="field-error">{{ fieldErrors.title }}</span>
      </label>

      <label class="field">
        <span class="field-label">分类</span>
        <select v-model="categoryCode" data-testid="post-category">
          <option v-for="category in categories" :key="category.code" :value="category.code">
            {{ category.name }}
          </option>
        </select>
        <span v-if="fieldErrors.category" class="field-error">{{ fieldErrors.category }}</span>
      </label>

      <label class="field">
        <span class="field-label">正文</span>
        <textarea v-model="content" data-testid="post-content" rows="8" placeholder="详细描述内容，最多 20000 字"></textarea>
        <span v-if="fieldErrors.content" class="field-error">{{ fieldErrors.content }}</span>
      </label>

      <label class="field">
        <span class="field-label">标签（可选，逗号分隔，最多 5 个）</span>
        <input v-model="tagsInput" data-testid="post-tags" placeholder="例如：文献管理, 科研工具" />
      </label>

      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />

      <div class="editor-actions">
        <button type="button" class="cancel" @click="visible = false">取消</button>
        <button type="button" class="submit" data-testid="post-submit" :disabled="submitting" @click="submit">
          {{ submitting ? '发布中…' : '发布' }}
        </button>
      </div>
    </div>
  </el-dialog>
</template>

<style scoped>
.editor-form { display: flex; flex-direction: column; gap: 16px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field-label { color: #344054; font-size: 13px; font-weight: 600; }
.field input, .field select, .field textarea { padding: 10px 12px; border: 1px solid #d0d5dd; border-radius: 9px; font: inherit; outline: none; }
.field input:focus, .field select:focus, .field textarea:focus { border-color: #4056ed; box-shadow: 0 0 0 3px rgba(64, 86, 237, .12); }
.field textarea { resize: vertical; line-height: 1.6; }
.field-error { color: #d92d20; font-size: 12px; }
.editor-actions { display: flex; justify-content: flex-end; gap: 10px; }
.editor-actions button { padding: 10px 18px; border-radius: 9px; font: inherit; font-weight: 700; cursor: pointer; }
.cancel { border: 1px solid #d0d5dd; color: #344054; background: #fff; }
.submit { border: 0; color: #fff; background: #4056ed; }
.submit:disabled { opacity: .6; cursor: not-allowed; }
</style>

