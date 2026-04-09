<script setup>
import { computed, ref } from 'vue'
import PaginationBar from '../../components/common/PaginationBar.vue'
import StatePanel from '../../components/common/StatePanel.vue'

const props = defineProps({
  records: {
    type: Array,
    required: true,
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

defineEmits(['retry'])

const searchKeyword = ref('')
const statusFilter = ref('ALL')
const currentPage = ref(1)
const pageSize = 10

const filteredRecords = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return props.records
    .filter((record) => {
      if (statusFilter.value !== 'ALL' && record.status !== statusFilter.value) {
        return false
      }

      if (!keyword) {
        return true
      }

      return [record.postTitle, record.siteName, record.message]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword))
    })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredRecords.value.length / pageSize)))
const pagedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredRecords.value.slice(start, start + pageSize)
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
      <h3>发布记录</h3>
      <div class="panel-stats"><span>总数 {{ records.length }}</span></div>
    </div>

    <div class="toolbar">
      <input
        v-model="searchKeyword"
        class="input toolbar-search"
        type="text"
        placeholder="搜索文章、站点或结果消息"
        @input="resetPage"
      >
      <select v-model="statusFilter" class="input toolbar-select" @change="resetPage">
        <option value="ALL">全部状态</option>
        <option value="SUCCESS">成功</option>
        <option value="FAILED">失败</option>
        <option value="RETRY_WAIT">等待重试</option>
      </select>
    </div>

    <StatePanel
      v-if="loading"
      title="发布记录加载中"
      description="正在同步发布历史。"
      tone="loading"
    />
    <StatePanel
      v-else-if="errorMessage"
      title="发布记录加载失败"
      :description="errorMessage"
      tone="error"
      action-label="重新加载"
      @action="$emit('retry')"
    />
    <StatePanel
      v-else-if="records.length === 0"
      title="暂无发布记录"
      description="先执行一次发布任务，记录才会出现在这里。"
      tone="empty"
    />
    <StatePanel
      v-else-if="filteredRecords.length === 0"
      title="没有匹配的发布记录"
      description="调整搜索词或状态筛选后再试一次。"
      tone="empty"
    />
    <div v-else class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>文章</th>
            <th>站点</th>
            <th>状态</th>
            <th>重试</th>
            <th>HTTP</th>
            <th>消息</th>
            <th>远端文章</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="record in pagedRecords" :key="record.publishId">
            <td>
              <div class="table-title">{{ record.postTitle }}</div>
              <div class="table-sub">#{{ record.postId }}</div>
            </td>
            <td>
              <div class="table-title">{{ record.siteName }}</div>
              <div class="table-sub">#{{ record.siteId }}</div>
            </td>
            <td>{{ record.status }}</td>
            <td>{{ record.retryCount }}</td>
            <td>{{ record.lastHttpStatus || '-' }}</td>
            <td>{{ record.message }}</td>
            <td>
              <a
                v-if="record.remotePostUrl"
                class="table-link"
                :href="record.remotePostUrl"
                target="_blank"
                rel="noreferrer"
              >
                {{ record.remotePostId }}
              </a>
              <span v-else>-</span>
            </td>
            <td>{{ record.createdAt }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <PaginationBar
      v-if="!loading && filteredRecords.length > 0"
      :current-page="currentPage"
      :total-items="filteredRecords.length"
      :total-pages="totalPages"
      @change="changePage"
    />
  </section>
</template>
