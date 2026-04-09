<script setup>
defineProps({
  currentPage: {
    type: Number,
    required: true,
  },
  totalPages: {
    type: Number,
    required: true,
  },
  totalItems: {
    type: Number,
    required: true,
  },
})

defineEmits(['change'])
</script>

<template>
  <div class="pagination-bar">
    <span class="pagination-summary">共 {{ totalItems }} 条，当前第 {{ currentPage }} / {{ totalPages }} 页</span>
    <div class="pagination-actions">
      <button
        class="button button-light"
        type="button"
        :disabled="currentPage <= 1"
        @click="$emit('change', currentPage - 1)"
      >
        上一页
      </button>
      <button
        class="button button-light"
        type="button"
        :disabled="currentPage >= totalPages"
        @click="$emit('change', currentPage + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>
