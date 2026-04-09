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
  posts: {
    type: Array,
    required: true,
  },
  sites: {
    type: Array,
    required: true,
  },
  results: {
    type: Array,
    required: true,
  },
})

defineEmits(['submit'])
</script>

<template>
  <section class="panel narrow-panel">
    <div class="panel-header"><h3>批量发布</h3></div>

    <form class="stack-form" @submit.prevent="$emit('submit')">
      <label class="field">
        <span class="field-label">选择文章</span>
        <select v-model="form.postId" class="input" :class="{ 'input-error': errors.postId }">
          <option value="">请选择文章</option>
          <option v-for="post in posts" :key="post.id" :value="post.id">
            {{ post.title }}
          </option>
        </select>
        <span v-if="errors.postId" class="field-error">{{ errors.postId }}</span>
      </label>

      <div class="field">
        <span class="field-label">选择站点</span>
        <div class="checkbox-grid">
          <label v-for="site in sites" :key="site.id" class="checkbox-card">
            <input v-model="form.siteIds" type="checkbox" :value="site.id">
            <div>
              <strong>{{ site.name }}</strong>
              <span>{{ site.baseUrl }}</span>
            </div>
          </label>
        </div>
        <span v-if="errors.siteIds" class="field-error">{{ errors.siteIds }}</span>
      </div>

      <div v-if="errors.form" class="form-error">{{ errors.form }}</div>

      <button class="button button-primary" type="submit" :disabled="loading">
        {{ loading ? '发布中' : '开始发布' }}
      </button>
    </form>
  </section>

  <section class="panel" v-if="results.length > 0">
    <div class="panel-header">
      <h3>发布结果</h3>
      <div class="panel-stats"><span>共 {{ results.length }} 条</span></div>
    </div>
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>站点</th>
            <th>状态</th>
            <th>消息</th>
            <th>远端文章</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="result in results" :key="result.publishId">
            <td>#{{ result.siteId }}</td>
            <td>{{ result.status }}</td>
            <td>{{ result.message }}</td>
            <td>
              <a v-if="result.remotePostUrl" class="table-link" :href="result.remotePostUrl" target="_blank" rel="noreferrer">
                {{ result.remotePostId }}
              </a>
              <span v-else>-</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
