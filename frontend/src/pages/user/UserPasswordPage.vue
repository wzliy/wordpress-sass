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

defineEmits(['change-password'])
</script>

<template>
  <section class="panel narrow-panel">
    <div class="panel-header"><h3>修改密码</h3></div>
    <form class="form-grid" @submit.prevent="$emit('change-password')">
      <label class="field">
        <span class="field-label">当前密码</span>
        <input v-model="form.currentPassword" class="input" :class="{ 'input-error': errors.currentPassword }" type="password">
        <span v-if="errors.currentPassword" class="field-error">{{ errors.currentPassword }}</span>
      </label>
      <label class="field">
        <span class="field-label">新密码</span>
        <input v-model="form.newPassword" class="input" :class="{ 'input-error': errors.newPassword }" type="password">
        <span v-if="errors.newPassword" class="field-error">{{ errors.newPassword }}</span>
      </label>
      <div v-if="errors.form" class="form-error span-all">{{ errors.form }}</div>
      <button class="button button-primary span-all" type="submit" :disabled="loading">
        {{ loading ? '更新中' : '更新密码' }}
      </button>
    </form>
  </section>
</template>
