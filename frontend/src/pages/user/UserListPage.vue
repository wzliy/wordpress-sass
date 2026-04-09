<script setup>
import { computed, ref } from 'vue'
import PaginationBar from '../../components/common/PaginationBar.vue'
import StatePanel from '../../components/common/StatePanel.vue'
const props = defineProps({
  loading: {
    type: Boolean,
    default: false,
  },
  userCount: {
    type: Number,
    default: 0,
  },
  activeUserCount: {
    type: Number,
    default: 0,
  },
  disabledUserCount: {
    type: Number,
    default: 0,
  },
  actionUserId: {
    type: [Number, null],
    default: null,
  },
  users: {
    type: Array,
    required: true,
  },
  errorMessage: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['retry', 'disable-user', 'enable-user', 'edit-user'])

const searchKeyword = ref('')
const roleFilter = ref('ALL')
const statusFilter = ref('ALL')
const sortMode = ref('created-desc')
const currentPage = ref(1)
const pageSize = 8

const filteredUsers = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return props.users
    .filter((user) => {
      if (roleFilter.value !== 'ALL' && user.role !== roleFilter.value) {
        return false
      }

      if (statusFilter.value !== 'ALL' && user.status !== statusFilter.value) {
        return false
      }

      if (!keyword) {
        return true
      }

      return [user.username, user.nickname, user.email]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword))
    })
    .sort((a, b) => {
      if (sortMode.value === 'username-asc') return String(a.username).localeCompare(String(b.username))
      if (sortMode.value === 'username-desc') return String(b.username).localeCompare(String(a.username))
      return Number(b.id || 0) - Number(a.id || 0)
    })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredUsers.value.length / pageSize)))
const pagedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredUsers.value.slice(start, start + pageSize)
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
      <h3>用户列表</h3>
      <div class="panel-stats">
        <span>总数 {{ userCount }}</span>
        <span>启用 {{ activeUserCount }}</span>
        <span>禁用 {{ disabledUserCount }}</span>
      </div>
    </div>

    <div class="toolbar">
      <input
        v-model="searchKeyword"
        class="input toolbar-search"
        type="text"
        placeholder="搜索用户名或邮箱"
        @input="resetPage"
      >
      <select v-model="roleFilter" class="input toolbar-select" @change="resetPage">
        <option value="ALL">全部角色</option>
        <option value="ADMIN">管理员</option>
      </select>
      <select v-model="statusFilter" class="input toolbar-select" @change="resetPage">
        <option value="ALL">全部状态</option>
        <option value="ACTIVE">启用</option>
        <option value="DISABLED">禁用</option>
      </select>
      <select v-model="sortMode" class="input toolbar-select" @change="resetPage">
        <option value="created-desc">最新优先</option>
        <option value="username-asc">用户名升序</option>
        <option value="username-desc">用户名降序</option>
      </select>
    </div>

    <StatePanel
      v-if="loading"
      title="用户数据加载中"
      description="正在获取当前租户下的用户列表。"
      tone="loading"
    />
    <StatePanel
      v-else-if="errorMessage"
      title="用户数据加载失败"
      :description="errorMessage"
      tone="error"
      action-label="重新加载"
      @action="$emit('retry')"
    />
    <StatePanel
      v-else-if="users.length === 0"
      title="暂无用户"
      description="当前租户下还没有其他管理员用户。"
      tone="empty"
    />
    <StatePanel
      v-else-if="filteredUsers.length === 0"
      title="没有匹配的用户"
      description="尝试调整搜索词或角色筛选。"
      tone="empty"
    />
    <div v-else class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>用户名</th>
            <th>昵称</th>
            <th>邮箱</th>
            <th>角色</th>
            <th>状态</th>
            <th>租户</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="account in pagedUsers" :key="account.id">
            <td>{{ account.username }}</td>
            <td>{{ account.nickname || '-' }}</td>
            <td>{{ account.email }}</td>
            <td>{{ account.role }}</td>
            <td>
              <span class="status-badge" :class="account.status === 'ACTIVE' ? 'status-ok' : 'status-off'">
                {{ account.status === 'ACTIVE' ? '启用' : '禁用' }}
              </span>
            </td>
            <td>#{{ account.tenantId }}</td>
            <td>{{ account.createdAt }}</td>
            <td class="table-actions">
              <button
                class="button button-light"
                type="button"
                :disabled="actionUserId === account.id"
                @click="emit('edit-user', account)"
              >
                编辑
              </button>
              <button
                v-if="account.status === 'ACTIVE'"
                class="button button-secondary"
                type="button"
                :disabled="actionUserId === account.id"
                @click="emit('disable-user', account)"
              >
                {{ actionUserId === account.id ? '处理中' : '禁用' }}
              </button>
              <button
                v-else
                class="button"
                type="button"
                :disabled="actionUserId === account.id"
                @click="emit('enable-user', account)"
              >
                {{ actionUserId === account.id ? '处理中' : '启用' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <PaginationBar
      v-if="!loading && filteredUsers.length > 0"
      :current-page="currentPage"
      :total-items="filteredUsers.length"
      :total-pages="totalPages"
      @change="changePage"
    />
  </section>
</template>
