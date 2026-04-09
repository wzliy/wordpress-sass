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

defineEmits(['update-user'])
</script>

<template>
  <section class="panel narrow-panel">
    <div class="panel-header"><h3>编辑用户资料</h3></div>
    <form class="form-grid" @submit.prevent="$emit('update-user')">
      <label class="field">
        <span class="field-label">用户名</span>
        <input :value="form.username" class="input input-readonly" type="text" readonly>
      </label>
      <label class="field">
        <span class="field-label">昵称</span>
        <input v-model="form.nickname" class="input" :class="{ 'input-error': errors.nickname }" type="text">
        <span v-if="errors.nickname" class="field-error">{{ errors.nickname }}</span>
      </label>
      <label class="field span-all">
        <span class="field-label">邮箱</span>
        <input v-model="form.email" class="input" :class="{ 'input-error': errors.email }" type="email">
        <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
      </label>
      <div v-if="errors.form" class="form-error span-all">{{ errors.form }}</div>
      <button class="button button-primary span-all" type="submit" :disabled="loading">
        {{ loading ? '保存中' : '保存资料' }}
      </button>
    </form>
  </section>
</template>
