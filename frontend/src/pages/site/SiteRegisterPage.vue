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

defineEmits(['register-site'])
</script>

<template>
  <section class="panel narrow-panel">
    <div class="panel-header"><h3>接入站点</h3></div>
    <form class="form-grid" @submit.prevent="$emit('register-site')">
      <label class="field">
        <span class="field-label">站点名称</span>
        <input v-model="form.name" class="input" :class="{ 'input-error': errors.name }" type="text">
        <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
      </label>
      <label class="field">
        <span class="field-label">站点地址</span>
        <input v-model="form.baseUrl" class="input" :class="{ 'input-error': errors.baseUrl }" type="url">
        <span v-if="errors.baseUrl" class="field-error">{{ errors.baseUrl }}</span>
      </label>
      <label class="field">
        <span class="field-label">WordPress 用户名</span>
        <input v-model="form.wpUsername" class="input" :class="{ 'input-error': errors.wpUsername }" type="text">
        <span v-if="errors.wpUsername" class="field-error">{{ errors.wpUsername }}</span>
      </label>
      <label class="field">
        <span class="field-label">应用密码</span>
        <input v-model="form.appPassword" class="input" :class="{ 'input-error': errors.appPassword }" type="password">
        <span v-if="errors.appPassword" class="field-error">{{ errors.appPassword }}</span>
      </label>
      <div v-if="errors.form" class="form-error span-all">{{ errors.form }}</div>
      <button class="button button-primary span-all" type="submit" :disabled="loading">
        {{ loading ? '保存中' : '创建站点' }}
      </button>
    </form>
  </section>
</template>
