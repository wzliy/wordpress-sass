<script setup>
import { computed, ref } from 'vue'
import PaginationBar from '../../components/common/PaginationBar.vue'
import StatePanel from '../../components/common/StatePanel.vue'
import { excerpt } from '../../utils/formatters'

const props = defineProps({
  posts: {
    type: Array,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  draftCount: {
    type: Number,
    default: 0,
  },
  postCount: {
    type: Number,
    default: 0,
  },
  errorMessage: {
    type: String,
    default: '',
  },
})

defineEmits(['retry'])

const searchKeyword = ref('')
const statusFilter = ref('ALL')
const sortMode = ref('created-desc')
const currentPage = ref(1)
const pageSize = 6

const filteredPosts = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return props.posts
    .filter((post) => {
      if (statusFilter.value !== 'ALL' && post.status !== statusFilter.value) {
        return false
      }

      if (!keyword) {
        return true
      }

      return [post.title, post.content]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword))
    })
    .sort((a, b) => {
      if (sortMode.value === 'title-asc') return String(a.title).localeCompare(String(b.title))
      if (sortMode.value === 'title-desc') return String(b.title).localeCompare(String(a.title))
      return Number(b.id || 0) - Number(a.id || 0)
    })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredPosts.value.length / pageSize)))
const pagedPosts = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredPosts.value.slice(start, start + pageSize)
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
      <h3>文章列表</h3>
      <div class="panel-stats">
        <span>总数 {{ postCount }}</span>
        <span>草稿 {{ draftCount }}</span>
      </div>
    </div>

    <div class="toolbar">
      <input
        v-model="searchKeyword"
        class="input toolbar-search"
        type="text"
        placeholder="搜索标题或内容"
        @input="resetPage"
      >
      <select v-model="statusFilter" class="input toolbar-select" @change="resetPage">
        <option value="ALL">全部状态</option>
        <option value="DRAFT">草稿</option>
      </select>
      <select v-model="sortMode" class="input toolbar-select" @change="resetPage">
        <option value="created-desc">最新优先</option>
        <option value="title-asc">标题升序</option>
        <option value="title-desc">标题降序</option>
      </select>
    </div>

    <StatePanel
      v-if="loading"
      title="文章数据加载中"
      description="正在同步文章列表。"
      tone="loading"
    />
    <StatePanel
      v-else-if="errorMessage"
      title="文章数据加载失败"
      :description="errorMessage"
      tone="error"
      action-label="重新加载"
      @action="$emit('retry')"
    />
    <StatePanel
      v-else-if="posts.length === 0"
      title="暂无文章"
      description="先创建一篇文章，后续再接入发布流程。"
      tone="empty"
    />
    <StatePanel
      v-else-if="filteredPosts.length === 0"
      title="没有匹配的文章"
      description="尝试调整搜索词或状态筛选。"
      tone="empty"
    />
    <div v-else class="card-grid">
      <article v-for="post in pagedPosts" :key="post.id" class="info-card">
        <div class="info-card-top">
          <span class="tag">{{ post.status }}</span>
          <span class="muted">#{{ post.id }}</span>
        </div>
        <h4>{{ post.title }}</h4>
        <p>{{ excerpt(post.content) }}</p>
        <div class="info-card-meta">
          <span>租户 {{ post.tenantId }}</span>
          <span>{{ post.createdAt }}</span>
        </div>
      </article>
    </div>
    <PaginationBar
      v-if="!loading && filteredPosts.length > 0"
      :current-page="currentPage"
      :total-items="filteredPosts.length"
      :total-pages="totalPages"
      @change="changePage"
    />
  </section>
</template>
