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
    <section class="login-stage">
      <div class="login-stage-copy">
        <span class="login-kicker">Control Access</span>
        <h1>进入暗色运营控制台。</h1>
        <p>围绕站点、发布、用户和建站流程组织的统一后台入口，先登录，再进入深色工作流。</p>
      </div>
      <div class="login-stage-metrics">
        <article class="login-stage-card">
          <span>Storefront</span>
          <strong>Host-driven</strong>
          <p>站点、域名和首页配置已经在同一条主线上收口。</p>
        </article>
        <article class="login-stage-card">
          <span>Design System</span>
          <strong>Dark-first</strong>
          <p>全局 token、壳组件和页面皮肤会统一遵循 `docs/design.md`。</p>
        </article>
      </div>
    </section>

    <div class="login-card">
      <div class="login-brand">
        <div class="login-logo">WS</div>
        <div>
          <h1>管理后台</h1>
          <p>Administrator Sign-in</p>
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
