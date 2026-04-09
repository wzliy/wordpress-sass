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

defineEmits(['provision-site'])
</script>

<template>
  <section class="panel narrow-panel">
    <div class="panel-header"><h3>自动建站</h3></div>
    <form class="form-grid" @submit.prevent="$emit('provision-site')">
      <label class="field">
        <span class="field-label">站点名称</span>
        <input v-model="form.name" class="input" :class="{ 'input-error': errors.name }" type="text">
        <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
      </label>
      <label class="field">
        <span class="field-label">管理员邮箱</span>
        <input v-model="form.adminEmail" class="input" :class="{ 'input-error': errors.adminEmail }" type="email">
        <span v-if="errors.adminEmail" class="field-error">{{ errors.adminEmail }}</span>
      </label>
      <label class="field span-all">
        <span class="field-label">子域名前缀</span>
        <input v-model="form.subdomainPrefix" class="input" :class="{ 'input-error': errors.subdomainPrefix }" type="text">
        <span v-if="errors.subdomainPrefix" class="field-error">{{ errors.subdomainPrefix }}</span>
      </label>
      <div v-if="errors.form" class="form-error span-all">{{ errors.form }}</div>
      <button class="button button-primary span-all" type="submit" :disabled="loading">
        {{ loading ? '建站中' : '发起建站' }}
      </button>
    </form>
  </section>
</template>
