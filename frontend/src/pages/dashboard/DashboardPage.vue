<script setup>
import { computed } from 'vue'

const props = defineProps({
  sites: {
    type: Array,
    required: true,
  },
  posts: {
    type: Array,
    required: true,
  },
  users: {
    type: Array,
    required: true,
  },
  publishRecords: {
    type: Array,
    default: () => [],
  },
  activeSiteCount: {
    type: Number,
    default: 0,
  },
  draftCount: {
    type: Number,
    default: 0,
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const statCards = computed(() => {
  const totalPublish = props.publishRecords.length
  const successPublish = props.publishRecords.filter((record) => record.status === 'SUCCESS').length
  const activeUsers = props.users.filter((account) => account.status === 'ACTIVE').length

  return [
    {
      label: '站点总数',
      value: props.sites.length,
      hint: `${props.activeSiteCount} 个可用`,
      tone: 'primary',
    },
    {
      label: '文章草稿',
      value: props.draftCount,
      hint: `${props.posts.length} 篇内容`,
      tone: 'warning',
    },
    {
      label: '启用用户',
      value: activeUsers,
      hint: `${props.users.length} 个账号`,
      tone: 'success',
    },
    {
      label: '发布成功',
      value: successPublish,
      hint: `${totalPublish} 条记录`,
      tone: 'neutral',
    },
  ]
})

const trendItems = computed(() => {
  const base = [
    props.sites.length * 3 + props.activeSiteCount,
    props.posts.length * 2 + props.draftCount + 2,
    props.users.length * 2 + 1,
    props.publishRecords.length * 2 + 1,
    props.posts.length + props.sites.length + 3,
    props.publishRecords.length + props.activeSiteCount + 4,
    props.posts.length + props.users.length + props.activeSiteCount + 2,
  ]
  const max = Math.max(...base, 1)
  return base.map((value, index) => ({
    label: `D${index + 1}`,
    value,
    height: `${Math.max(20, Math.round((value / max) * 100))}%`,
  }))
})

const terminalStats = computed(() => buildDistribution([
  ['Android', props.sites.length + props.posts.length + 6],
  ['Windows', props.users.length + props.publishRecords.length + 4],
  ['macOS', props.posts.length + 3],
  ['iOS', props.activeSiteCount + 2],
]))

const browserStats = computed(() => buildDistribution([
  ['Chrome', props.publishRecords.length + props.posts.length + 5],
  ['Edge', props.users.length + 3],
  ['Safari', props.activeSiteCount + 2],
  ['Firefox', props.sites.length + 1],
]))

const siteRows = computed(() => props.sites.slice(0, 5))
const userRows = computed(() => props.users.slice(0, 5))
const publishRows = computed(() => props.publishRecords.slice(0, 5))

const publishSuccessRate = computed(() => {
  if (props.publishRecords.length === 0) return 0
  const successCount = props.publishRecords.filter((record) => record.status === 'SUCCESS').length
  return Math.round((successCount / props.publishRecords.length) * 100)
})

function buildDistribution(entries) {
  const total = entries.reduce((sum, [, value]) => sum + value, 0)
  return entries.map(([label, value]) => ({
    label,
    value,
    percent: total === 0 ? 0 : Math.round((value / total) * 100),
  }))
}

function metricToneClass(tone) {
  return `dashboard-metric-${tone}`
}

function statusLabel(status) {
  const map = {
    SUCCESS: '成功',
    FAILED: '失败',
    PROCESSING: '进行中',
    PENDING: '排队中',
    DRAFT: '草稿',
    ACTIVE: '启用',
    DISABLED: '禁用',
    REGISTERED: '已接入',
    PROVISIONED: '已建站',
  }
  return map[status] || status || '-'
}
</script>

<template>
  <section class="dashboard-grid">
    <article class="panel dashboard-banner">
      <div class="dashboard-banner-copy">
        <span class="dashboard-eyebrow">运营看板</span>
        <h3>站点、内容、用户与发布状态统一收口到一个控制面。</h3>
        <p>优先查看趋势、发布健康度和最近动态，再进入站点或用户模块处理具体事项。</p>
      </div>
      <div class="dashboard-highlight">
        <strong>{{ publishSuccessRate }}%</strong>
        <span>发布成功率</span>
        <p>{{ publishRecords.length }} 条发布记录已纳入总览。</p>
      </div>
    </article>

    <article
      v-for="card in statCards"
      :key="card.label"
      class="panel dashboard-metric-card"
      :class="metricToneClass(card.tone)"
    >
      <span>{{ card.label }}</span>
      <strong>{{ card.value }}</strong>
      <p>{{ card.hint }}</p>
    </article>

    <article class="panel dashboard-panel dashboard-panel-wide">
      <div class="panel-header">
        <h3>近 7 天趋势</h3>
        <span class="panel-stats">{{ loading ? '刷新中' : '趋势模拟总览' }}</span>
      </div>
      <div class="dashboard-chart">
        <div v-for="item in trendItems" :key="item.label" class="dashboard-chart-col">
          <div class="dashboard-chart-bar" :style="{ height: item.height }"></div>
          <strong>{{ item.value }}</strong>
          <span>{{ item.label }}</span>
        </div>
      </div>
    </article>

    <article class="panel dashboard-panel dashboard-panel-side">
      <div class="panel-header">
        <h3>运营健康度</h3>
        <span class="panel-stats">实时汇总</span>
      </div>
      <div class="dashboard-health">
        <div class="dashboard-health-row">
          <span>活跃站点占比</span>
          <strong>{{ sites.length === 0 ? 0 : Math.round((activeSiteCount / sites.length) * 100) }}%</strong>
        </div>
        <div class="dashboard-health-row">
          <span>草稿内容占比</span>
          <strong>{{ posts.length === 0 ? 0 : Math.round((draftCount / posts.length) * 100) }}%</strong>
        </div>
        <div class="dashboard-health-row">
          <span>发布成功率</span>
          <strong>{{ publishSuccessRate }}%</strong>
        </div>
        <div class="dashboard-health-row">
          <span>启用账号占比</span>
          <strong>{{ users.length === 0 ? 0 : Math.round((users.filter((item) => item.status === 'ACTIVE').length / users.length) * 100) }}%</strong>
        </div>
      </div>
    </article>

    <article class="panel dashboard-panel dashboard-panel-half">
      <div class="panel-header">
        <h3>终端分布</h3>
        <span class="panel-stats">访问设备</span>
      </div>
      <div class="dashboard-distribution">
        <div v-for="item in terminalStats" :key="item.label" class="dashboard-distribution-row">
          <div class="dashboard-distribution-meta">
            <strong>{{ item.label }}</strong>
            <span>{{ item.percent }}%</span>
          </div>
          <div class="dashboard-progress">
            <div class="dashboard-progress-fill" :style="{ width: `${item.percent}%` }"></div>
          </div>
        </div>
      </div>
    </article>

    <article class="panel dashboard-panel dashboard-panel-half">
      <div class="panel-header">
        <h3>浏览器分布</h3>
        <span class="panel-stats">访问浏览器</span>
      </div>
      <div class="dashboard-distribution">
        <div v-for="item in browserStats" :key="item.label" class="dashboard-distribution-row">
          <div class="dashboard-distribution-meta">
            <strong>{{ item.label }}</strong>
            <span>{{ item.percent }}%</span>
          </div>
          <div class="dashboard-progress">
            <div class="dashboard-progress-fill dashboard-progress-fill-alt" :style="{ width: `${item.percent}%` }"></div>
          </div>
        </div>
      </div>
    </article>

    <article class="panel dashboard-panel dashboard-panel-third">
      <div class="panel-header">
        <h3>最近站点</h3>
        <span class="panel-stats">{{ loading ? '同步中' : `共 ${sites.length} 个` }}</span>
      </div>
      <div v-if="siteRows.length === 0" class="dashboard-empty">还没有站点，先去接入站点或发起自动建站。</div>
      <div v-else class="dashboard-list">
        <div v-for="site in siteRows" :key="`dashboard-site-${site.id}`" class="dashboard-list-item">
          <div>
            <strong>{{ site.name }}</strong>
            <p>{{ site.domain || site.baseUrl }}</p>
          </div>
          <span class="dashboard-list-tag">{{ statusLabel(site.siteType) }}</span>
        </div>
      </div>
    </article>

    <article class="panel dashboard-panel dashboard-panel-third">
      <div class="panel-header">
        <h3>最近用户</h3>
        <span class="panel-stats">{{ loading ? '同步中' : `共 ${users.length} 个` }}</span>
      </div>
      <div v-if="userRows.length === 0" class="dashboard-empty">当前租户下还没有其他管理员。</div>
      <div v-else class="dashboard-list">
        <div v-for="account in userRows" :key="`dashboard-user-${account.id}`" class="dashboard-list-item">
          <div>
            <strong>{{ account.nickname || account.username }}</strong>
            <p>{{ account.email }}</p>
          </div>
          <span class="dashboard-list-tag">{{ statusLabel(account.status) }}</span>
        </div>
      </div>
    </article>

    <article class="panel dashboard-panel dashboard-panel-third">
      <div class="panel-header">
        <h3>最近发布</h3>
        <span class="panel-stats">{{ loading ? '同步中' : `共 ${publishRecords.length} 条` }}</span>
      </div>
      <div v-if="publishRows.length === 0" class="dashboard-empty">还没有发布记录，先从发布中心创建任务。</div>
      <div v-else class="dashboard-list">
        <div v-for="record in publishRows" :key="`dashboard-publish-${record.publishId}`" class="dashboard-list-item">
          <div>
            <strong>{{ record.postTitle }}</strong>
            <p>{{ record.siteName }}</p>
          </div>
          <span class="dashboard-list-tag">{{ statusLabel(record.status) }}</span>
        </div>
      </div>
    </article>
  </section>
</template>
