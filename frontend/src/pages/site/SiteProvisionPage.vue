<script setup>
import StatePanel from '../../components/common/StatePanel.vue'

const countryOptions = [
  { value: 'US', label: '美国' },
  { value: 'GB', label: '英国' },
  { value: 'DE', label: '德国' },
]

const languageOptions = [
  { value: 'en', label: 'English' },
  { value: 'de', label: 'Deutsch' },
  { value: 'fr', label: 'Francais' },
]

const currencyOptions = [
  { value: 'USD', label: 'USD' },
  { value: 'EUR', label: 'EUR' },
  { value: 'GBP', label: 'GBP' },
]

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
  templates: {
    type: Array,
    default: () => [],
  },
  templateLoading: {
    type: Boolean,
    default: false,
  },
  templateError: {
    type: String,
    default: '',
  },
})

defineEmits(['provision-site'])
</script>

<template>
  <section class="provision-layout">
    <article class="panel provision-template-panel">
      <div class="panel-header">
        <div>
          <h3>选择模板</h3>
          <p class="panel-description">先确定站点骨架，再填写最少建站信息。</p>
        </div>
        <span class="panel-stats">{{ templates.length }} 个模板</span>
      </div>

      <StatePanel
        v-if="templateLoading"
        title="模板加载中"
        description="正在同步当前可用模板。"
        tone="loading"
      />
      <StatePanel
        v-else-if="templateError"
        title="模板加载失败"
        :description="templateError"
        tone="error"
      />
      <StatePanel
        v-else-if="templates.length === 0"
        title="暂无可用模板"
        description="请先准备模板数据，或检查模板初始化逻辑。"
        tone="empty"
      />
      <div v-else class="template-card-grid">
        <label
          v-for="template in templates"
          :key="template.code"
          class="template-card"
          :class="{ 'template-card-active': form.templateCode === template.code }"
        >
          <input v-model="form.templateCode" class="template-card-input" type="radio" :value="template.code">
          <span class="template-card-kicker">{{ template.category || '默认模板' }}</span>
          <strong>{{ template.name }}</strong>
          <p>{{ template.description }}</p>
          <div class="template-card-meta">
            <span>{{ template.siteType }}</span>
            <span>{{ template.builtIn ? '内置模板' : '租户模板' }}</span>
          </div>
        </label>
      </div>
      <span v-if="errors.templateCode" class="field-error">{{ errors.templateCode }}</span>
    </article>

    <article class="panel provision-form-panel">
      <div class="panel-header">
        <div>
          <h3>自动建站</h3>
          <p class="panel-description">提交后会创建站点、写入模板绑定，并自动进入工作台。</p>
        </div>
      </div>
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
        <label class="field">
          <span class="field-label">国家</span>
          <select v-model="form.countryCode" class="input" :class="{ 'input-error': errors.countryCode }">
            <option v-for="option in countryOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
          </select>
          <span v-if="errors.countryCode" class="field-error">{{ errors.countryCode }}</span>
        </label>
        <label class="field">
          <span class="field-label">语言</span>
          <select v-model="form.languageCode" class="input" :class="{ 'input-error': errors.languageCode }">
            <option v-for="option in languageOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
          </select>
          <span v-if="errors.languageCode" class="field-error">{{ errors.languageCode }}</span>
        </label>
        <label class="field">
          <span class="field-label">币种</span>
          <select v-model="form.currencyCode" class="input" :class="{ 'input-error': errors.currencyCode }">
            <option v-for="option in currencyOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
          </select>
          <span v-if="errors.currencyCode" class="field-error">{{ errors.currencyCode }}</span>
        </label>
        <label class="field span-all">
          <span class="field-label">子域名前缀</span>
          <input v-model="form.subdomainPrefix" class="input" :class="{ 'input-error': errors.subdomainPrefix }" type="text">
          <span v-if="errors.subdomainPrefix" class="field-error">{{ errors.subdomainPrefix }}</span>
        </label>
        <div class="provision-summary span-all">
          <span class="workspace-eyebrow">自动初始化</span>
          <ul>
            <li>通过 Multisite 创建站点和后台地址</li>
            <li>把模板绑定保存到站点记录，供工作台读取</li>
            <li>初始化页面骨架、主题变量和默认支付/追踪配置</li>
            <li>建站完成后直接跳转到该站点工作台</li>
          </ul>
        </div>
        <div v-if="errors.form" class="form-error span-all">{{ errors.form }}</div>
        <button class="button button-primary span-all" type="submit" :disabled="loading || templateLoading || templates.length === 0">
          {{ loading ? '建站中' : '发起建站' }}
        </button>
      </form>
    </article>
  </section>
</template>
