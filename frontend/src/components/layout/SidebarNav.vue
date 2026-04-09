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
})

defineEmits(['change-module'])
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-brand">
      <div class="sidebar-logo">WS</div>
      <div>
        <h1>控制台</h1>
        <p>WordPress SaaS</p>
      </div>
    </div>

    <nav class="sidebar-nav">
      <button
        v-for="item in MODULE_ITEMS"
        :key="item.key"
        class="sidebar-nav-item"
        :class="{ 'sidebar-nav-item-active': currentModule === item.key }"
        @click="$emit('change-module', item.key)"
      >
        {{ item.label }}
      </button>
    </nav>

    <div class="sidebar-summary">
      <div v-for="item in SUMMARY_ITEMS" :key="`${item.key}-summary`" class="summary-card">
        <span>{{ item.label }}</span>
        <strong>{{ counts[item.key] }}</strong>
      </div>
    </div>
  </aside>
</template>
