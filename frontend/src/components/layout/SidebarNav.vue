<script setup>
import { MODULE_ITEMS, SUMMARY_ITEMS } from '../../config/navigation'

defineProps({
  counts: {
    type: Object,
    required: true,
  },
  currentModule: {
    type: String,
    required: true,
  },
  modulePresentation: {
    type: Object,
    required: true,
  },
  user: {
    type: Object,
    default: null,
  },
})

defineEmits(['change-module', 'close'])
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-glow" />
    <div class="sidebar-brand">
      <div class="sidebar-logo">WS</div>
      <div>
        <h1>Commerce Console</h1>
        <p>{{ modulePresentation.accentLabel }}</p>
      </div>
    </div>

    <div class="sidebar-context">
      <span>{{ modulePresentation.eyebrow }}</span>
      <strong>{{ modulePresentation.title }}</strong>
      <p>{{ modulePresentation.description }}</p>
    </div>

    <nav class="sidebar-nav">
      <button
        v-for="item in MODULE_ITEMS"
        :key="item.key"
        class="sidebar-nav-item"
        :class="{ 'sidebar-nav-item-active': currentModule === item.key }"
        @click="$emit('change-module', item.key)"
      >
        <span class="sidebar-nav-glyph">{{ item.glyph }}</span>
        <span class="sidebar-nav-copy">
          <strong>{{ item.label }}</strong>
          <span>{{ item.description }}</span>
        </span>
        <span v-if="item.countKey" class="sidebar-nav-count">{{ counts[item.countKey] ?? 0 }}</span>
      </button>
    </nav>

    <div class="sidebar-summary">
      <div v-for="item in SUMMARY_ITEMS" :key="`${item.key}-summary`" class="summary-card">
        <span>{{ item.label }}</span>
        <strong>{{ counts[item.key] }}</strong>
      </div>
    </div>

    <div class="sidebar-user-card">
      <span>当前用户</span>
      <strong>{{ user?.nickname || user?.username || '未登录' }}</strong>
      <p>{{ user?.role || 'ADMIN' }} · Tenant #{{ user?.tenantId || '--' }}</p>
    </div>
  </aside>
</template>
