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
  notice: {
    type: Object,
    required: true,
  },
})

defineEmits(['submit'])
</script>

<template>
  <div class="login-layout">
    <div class="login-card">
      <div class="login-brand">
        <div class="login-logo">WS</div>
        <div>
          <h1>管理后台</h1>
          <p>管理员登录</p>
        </div>
      </div>

      <form class="login-form" @submit.prevent="$emit('submit')">
        <label class="field">
          <span class="field-label">用户名</span>
          <input v-model="form.username" class="input" :class="{ 'input-error': errors.username }" type="text">
          <span v-if="errors.username" class="field-error">{{ errors.username }}</span>
        </label>
        <label class="field">
          <span class="field-label">密码</span>
          <input v-model="form.password" class="input" :class="{ 'input-error': errors.password }" type="password">
          <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
        </label>
        <div v-if="errors.form" class="form-error">{{ errors.form }}</div>
        <button class="button button-primary button-block" type="submit" :disabled="loading">
          {{ loading ? '登录中' : '登录' }}
        </button>
      </form>

      <div class="login-notice" :class="`notice-${notice.type}`">{{ notice.message }}</div>
    </div>
  </div>
</template>
