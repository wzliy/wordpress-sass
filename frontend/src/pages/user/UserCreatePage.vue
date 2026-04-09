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

defineEmits(['create-user'])
</script>

<template>
  <section class="panel narrow-panel">
    <div class="panel-header"><h3>新增用户</h3></div>
    <form class="form-grid" @submit.prevent="$emit('create-user')">
      <label class="field">
        <span class="field-label">用户名</span>
        <input v-model="form.username" class="input" :class="{ 'input-error': errors.username }" type="text">
        <span v-if="errors.username" class="field-error">{{ errors.username }}</span>
      </label>
      <label class="field">
        <span class="field-label">邮箱</span>
        <input v-model="form.email" class="input" :class="{ 'input-error': errors.email }" type="email">
        <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
      </label>
      <label class="field span-all">
        <span class="field-label">初始密码</span>
        <input v-model="form.password" class="input" :class="{ 'input-error': errors.password }" type="password">
        <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
      </label>
      <div v-if="errors.form" class="form-error span-all">{{ errors.form }}</div>
      <button class="button button-primary span-all" type="submit" :disabled="loading">
        {{ loading ? '保存中' : '创建用户' }}
      </button>
    </form>
  </section>
</template>
