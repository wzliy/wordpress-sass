<script setup>
defineProps({
  form: {
    type: Object,
    required: true,
  },
  errors: {
    type: Object,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['create-post'])
</script>

<template>
  <section class="panel narrow-panel">
    <div class="panel-header"><h3>新建文章</h3></div>
    <form class="stack-form" @submit.prevent="$emit('create-post')">
      <label class="field">
        <span class="field-label">标题</span>
        <input v-model="form.title" class="input" :class="{ 'input-error': errors.title }" type="text">
        <span v-if="errors.title" class="field-error">{{ errors.title }}</span>
      </label>
      <label class="field">
        <span class="field-label">内容</span>
        <textarea v-model="form.content" class="textarea" :class="{ 'input-error': errors.content }" />
        <span v-if="errors.content" class="field-error">{{ errors.content }}</span>
      </label>
      <div v-if="errors.form" class="form-error">{{ errors.form }}</div>
      <button class="button button-primary" type="submit" :disabled="loading">
        {{ loading ? '保存中' : '创建文章' }}
      </button>
    </form>
  </section>
</template>
