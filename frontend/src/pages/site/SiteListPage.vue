<script setup>
import { computed, ref } from 'vue'
import PaginationBar from '../../components/common/PaginationBar.vue'
import StatePanel from '../../components/common/StatePanel.vue'
import { siteStatusClass, siteStatusLabel } from '../../utils/formatters'

const props = defineProps({
  sites: {
    type: Array,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  activeSiteCount: {
    type: Number,
    default: 0,
  },
  siteCount: {
    type: Number,
    default: 0,
  },
  testId: {
    type: Number,
    default: null,
  },
  errorMessage: {
    type: String,
    default: '',
  },
})

defineEmits(['retry', 'test-site'])

const searchKeyword = ref('')
const typeFilter = ref('ALL')
const statusFilter = ref('ALL')
const sortMode = ref('created-desc')
const currentPage = ref(1)
const pageSize = 8

const filteredSites = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return props.sites
    .filter((site) => {
      if (typeFilter.value !== 'ALL' && site.siteType !== typeFilter.value) {
        return false
      }

      const status = site.provisionStatus === 'PROVISIONING' ? 'PROVISIONING' : site.status === 1 ? 'ACTIVE' : 'INACTIVE'
      if (statusFilter.value !== 'ALL' && status !== statusFilter.value) {
        return false
      }

      if (!keyword) {
        return true
      }

      return [site.name, site.baseUrl, site.domain, site.wpUsername]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword))
    })
    .sort((a, b) => {
      if (sortMode.value === 'name-asc') return String(a.name).localeCompare(String(b.name))
      if (sortMode.value === 'name-desc') return String(b.name).localeCompare(String(a.name))
      return Number(b.id || 0) - Number(a.id || 0)
    })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredSites.value.length / pageSize)))
const pagedSites = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredSites.value.slice(start, start + pageSize)
})

function resetPage() {
  currentPage.value = 1
}

function changePage(page) {
  currentPage.value = Math.min(Math.max(1, page), totalPages.value)
}
</script>

<template>
  <section class="panel">
    <div class="panel-header">
      <h3>站点列表</h3>
      <div class="panel-stats">
        <span>总数 {{ siteCount }}</span>
        <span>可用 {{ activeSiteCount }}</span>
      </div>
    </div>

    <div class="toolbar">
      <input
        v-model="searchKeyword"
        class="input toolbar-search"
        type="text"
        placeholder="搜索站点名称、域名或账号"
        @input="resetPage"
      >
      <select v-model="typeFilter" class="input toolbar-select" @change="resetPage">
        <option value="ALL">全部类型</option>
        <option value="REGISTERED">已接入</option>
        <option value="PROVISIONED">自动建站</option>
      </select>
      <select v-model="statusFilter" class="input toolbar-select" @change="resetPage">
        <option value="ALL">全部状态</option>
        <option value="ACTIVE">可用</option>
        <option value="PROVISIONING">建站中</option>
        <option value="INACTIVE">停用</option>
      </select>
      <select v-model="sortMode" class="input toolbar-select" @change="resetPage">
        <option value="created-desc">最新优先</option>
        <option value="name-asc">名称升序</option>
        <option value="name-desc">名称降序</option>
      </select>
    </div>

    <StatePanel
      v-if="loading"
      title="站点数据加载中"
      description="正在同步当前租户下的站点信息。"
      tone="loading"
    />
    <StatePanel
      v-else-if="errorMessage"
      title="站点数据加载失败"
      :description="errorMessage"
      tone="error"
      action-label="重新加载"
      @action="$emit('retry')"
    />
    <StatePanel
      v-else-if="sites.length === 0"
      title="暂无站点"
      description="先接入已有站点，或通过自动建站创建第一个站点。"
      tone="empty"
    />
    <StatePanel
      v-else-if="filteredSites.length === 0"
      title="没有匹配的站点"
      description="调整筛选条件后再试一次。"
      tone="empty"
    />
    <div v-else class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>名称</th>
            <th>类型</th>
            <th>状态</th>
            <th>域名</th>
            <th>后台地址</th>
            <th>用户</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="site in pagedSites" :key="site.id">
            <td>
              <div class="table-title">{{ site.name }}</div>
              <div class="table-sub">{{ site.baseUrl }}</div>
            </td>
            <td>{{ site.siteType }}</td>
            <td>
              <span class="status-pill" :class="siteStatusClass(site)">{{ siteStatusLabel(site) }}</span>
            </td>
            <td>{{ site.domain || '-' }}</td>
            <td>{{ site.adminUrl || '-' }}</td>
            <td>{{ site.wpUsername }}</td>
            <td class="actions-cell">
              <button class="table-link" @click="$emit('test-site', site)" :disabled="testId === site.id">
                {{ testId === site.id ? '测试中' : '测试' }}
              </button>
              <a class="table-link" :href="site.adminUrl || site.baseUrl" target="_blank" rel="noreferrer">打开</a>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <PaginationBar
      v-if="!loading && filteredSites.length > 0"
      :current-page="currentPage"
      :total-items="filteredSites.length"
      :total-pages="totalPages"
      @change="changePage"
    />
  </section>
</template>
