<script setup>
import StatePanel from '../../components/common/StatePanel.vue'

const props = defineProps({
  workspace: {
    type: Object,
    default: null,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  errorMessage: {
    type: String,
    default: '',
  },
})

function workspaceTone(status) {
  const toneMap = {
    ACTIVE: 'success',
    ACTION_REQUIRED: 'warn',
    CREATING: 'warn',
    AT_RISK: 'danger',
    DISABLED: 'danger',
    ARCHIVED: 'neutral',
  }
  return toneMap[status] || 'neutral'
}

function workspaceLabel(status) {
  const labelMap = {
    ACTIVE: '可运营',
    ACTION_REQUIRED: '待补配置',
    CREATING: '建站中',
    AT_RISK: '存在风险',
    DISABLED: '当前停用',
    ARCHIVED: '已归档',
  }
  return labelMap[status] || status || '-'
}

function readinessLabel(level) {
  const labelMap = {
    READY: '已就绪',
    BASIC_READY: '基础可用',
    NOT_READY: '未就绪',
    RISK: '存在风险',
  }
  return labelMap[level] || level || '-'
}

function alertClass(level) {
  return `workspace-alert-${String(level || '').toLowerCase()}`
}

function moduleClass(status) {
  return `workspace-module-${String(status || '').toLowerCase()}`
}

function metricToneClass(tone) {
  return tone ? `workspace-metric-${tone}` : ''
}

function actionClass(action) {
  if (!action.enabled) return 'button button-muted'
  return action.type === 'EXTERNAL' ? 'button button-secondary' : 'button button-primary'
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ')
}
</script>

<template>
  <section class="workspace-page">
    <StatePanel
      v-if="loading"
      title="工作台加载中"
      description="正在聚合站点概览、准备度和模块摘要。"
      tone="loading"
    />
    <StatePanel
      v-else-if="errorMessage"
      title="工作台加载失败"
      :description="errorMessage"
      tone="error"
    />
    <StatePanel
      v-else-if="!workspace"
      title="工作台暂无数据"
      description="当前站点还没有可展示的工作台信息。"
      tone="empty"
    />
    <template v-else>
      <article class="panel workspace-hero">
        <div class="workspace-hero-main">
          <div class="workspace-hero-meta">
            <span class="workspace-eyebrow">站点工作台</span>
            <span class="status-pill" :class="`status-${workspaceTone(workspace.workspaceStatus)}`">
              {{ workspaceLabel(workspace.workspaceStatus) }}
            </span>
          </div>
          <h3>{{ workspace.profile.name }}</h3>
          <p>{{ workspace.profile.domain || workspace.profile.baseUrl }}</p>
          <div class="workspace-profile-grid">
            <div>
              <span>站点类型</span>
              <strong>{{ workspace.profile.siteType }}</strong>
            </div>
            <div>
              <span>站点状态</span>
              <strong>{{ workspace.profile.status }}</strong>
            </div>
            <div>
              <span>建站状态</span>
              <strong>{{ workspace.profile.provisionStatus }}</strong>
            </div>
            <div>
              <span>创建时间</span>
              <strong>{{ formatTime(workspace.profile.createdAt) }}</strong>
            </div>
          </div>
        </div>
        <div class="workspace-hero-side">
          <div class="workspace-score-card">
            <span>可运营准备度</span>
            <strong>{{ workspace.readiness.score }}</strong>
            <p>{{ readinessLabel(workspace.readiness.level) }}</p>
          </div>
          <div class="workspace-actions">
            <template v-for="action in workspace.quickActions" :key="action.code">
              <a
                v-if="action.enabled && action.type === 'EXTERNAL'"
                class="button"
                :class="actionClass(action)"
                :href="action.path"
                target="_blank"
                rel="noreferrer"
              >
                {{ action.label }}
              </a>
              <RouterLink
                v-else-if="action.enabled && action.type === 'INTERNAL'"
                class="button"
                :class="actionClass(action)"
                :to="action.path"
              >
                {{ action.label }}
              </RouterLink>
              <button v-else class="button button-muted" type="button" disabled>{{ action.label }}</button>
            </template>
          </div>
        </div>
      </article>

      <div v-if="workspace.alerts.length > 0" class="workspace-alerts">
        <article
          v-for="alert in workspace.alerts"
          :key="`${alert.code}-${alert.createdAt}`"
          class="panel workspace-alert"
          :class="alertClass(alert.level)"
        >
          <div>
            <strong>{{ alert.title }}</strong>
            <p>{{ alert.message }}</p>
          </div>
          <span>{{ alert.level }}</span>
        </article>
      </div>

      <div class="workspace-grid">
        <article class="panel workspace-section">
          <div class="panel-header">
            <h3>准备度检查</h3>
            <span class="panel-stats">{{ readinessLabel(workspace.readiness.level) }}</span>
          </div>
          <div class="workspace-readiness">
            <div
              v-for="item in workspace.readiness.items"
              :key="item.code"
              class="workspace-readiness-item"
            >
              <div>
                <strong>{{ item.label }}</strong>
                <p>{{ item.message }}</p>
              </div>
              <span class="status-pill" :class="`status-${workspaceTone(item.status === 'DONE' ? 'ACTIVE' : item.status === 'WARNING' ? 'AT_RISK' : 'ACTION_REQUIRED')}`">
                {{ item.status }}
              </span>
            </div>
          </div>
        </article>

        <article class="panel workspace-section">
          <div class="panel-header">
            <h3>待处理事项</h3>
            <span class="panel-stats">{{ workspace.pendingTasks.length }} 条任务</span>
          </div>
          <div v-if="workspace.pendingTasks.length === 0" class="workspace-empty">
            当前没有挂起任务，站点可以继续进入下一步配置。
          </div>
          <div v-else class="workspace-task-list">
            <div
              v-for="task in workspace.pendingTasks"
              :key="`${task.taskType}-${task.startedAt}`"
              class="workspace-task-item"
            >
              <div>
                <strong>{{ task.title }}</strong>
                <p>{{ task.message }}</p>
              </div>
              <span>{{ task.status }}</span>
            </div>
          </div>
        </article>
      </div>

      <section class="workspace-modules">
        <article
          v-for="module in workspace.moduleSummaries"
          :key="module.module"
          class="panel workspace-module-card"
          :class="moduleClass(module.status)"
        >
          <div class="workspace-module-head">
            <div>
              <span class="workspace-module-kicker">{{ module.module }}</span>
              <h3>{{ module.title }}</h3>
            </div>
            <span class="status-pill" :class="`status-${workspaceTone(module.status === 'READY' ? 'ACTIVE' : module.status === 'CONFIGURING' ? 'CREATING' : module.status === 'DISABLED' ? 'DISABLED' : 'ACTION_REQUIRED')}`">
              {{ module.status }}
            </span>
          </div>
          <div class="workspace-module-primary">
            <span>{{ module.primaryMetric.label }}</span>
            <strong :class="metricToneClass(module.primaryMetric.tone)">{{ module.primaryMetric.value }}</strong>
          </div>
          <div class="workspace-module-metrics">
            <div v-for="metric in module.secondaryMetrics" :key="`${module.module}-${metric.label}`">
              <span>{{ metric.label }}</span>
              <strong>{{ metric.value }}</strong>
            </div>
          </div>
          <ul class="workspace-highlights">
            <li v-for="highlight in module.highlights" :key="highlight">{{ highlight }}</li>
          </ul>
          <div class="workspace-module-actions">
            <template v-for="action in module.actions" :key="action.code">
              <a
                v-if="action.enabled && action.type === 'EXTERNAL'"
                class="button"
                :class="actionClass(action)"
                :href="action.path"
                target="_blank"
                rel="noreferrer"
              >
                {{ action.label }}
              </a>
              <RouterLink
                v-else-if="action.enabled && action.type === 'INTERNAL'"
                class="button"
                :class="actionClass(action)"
                :to="action.path"
              >
                {{ action.label }}
              </RouterLink>
              <button v-else class="button button-muted" type="button" disabled>{{ action.label }}</button>
            </template>
          </div>
        </article>
      </section>

      <div class="workspace-grid">
        <article class="panel workspace-section">
          <div class="panel-header">
            <h3>最近活动</h3>
            <span class="panel-stats">{{ workspace.recentActivities.length }} 条</span>
          </div>
          <div v-if="workspace.recentActivities.length === 0" class="workspace-empty">
            当前还没有可展示的活动流。
          </div>
          <div v-else class="workspace-activity-list">
            <div
              v-for="activity in workspace.recentActivities"
              :key="`${activity.type}-${activity.occurredAt}`"
              class="workspace-activity-item"
            >
              <div>
                <strong>{{ activity.title }}</strong>
                <p>{{ activity.description }}</p>
              </div>
              <span>{{ formatTime(activity.occurredAt) }}</span>
            </div>
          </div>
        </article>

        <article class="panel workspace-section">
          <div class="panel-header">
            <h3>基础配置</h3>
            <span class="panel-stats">站点档案</span>
          </div>
          <dl class="workspace-base-info">
            <div>
              <dt>访问地址</dt>
              <dd>{{ workspace.profile.baseUrl || '-' }}</dd>
            </div>
            <div>
              <dt>后台地址</dt>
              <dd>{{ workspace.profile.adminUrl || '-' }}</dd>
            </div>
            <div>
              <dt>模板</dt>
              <dd>{{ workspace.profile.templateName || '待绑定' }}</dd>
            </div>
            <div>
              <dt>国家 / 语言 / 币种</dt>
              <dd>{{ [workspace.profile.countryCode, workspace.profile.languageCode, workspace.profile.currencyCode].filter(Boolean).join(' / ') || '待配置' }}</dd>
            </div>
            <div>
              <dt>状态摘要</dt>
              <dd>{{ workspace.profile.statusMessage || '暂无额外状态信息' }}</dd>
            </div>
            <div>
              <dt>聚合时间</dt>
              <dd>{{ formatTime(workspace.generatedAt) }}</dd>
            </div>
          </dl>
        </article>
      </div>
    </template>
  </section>
</template>
