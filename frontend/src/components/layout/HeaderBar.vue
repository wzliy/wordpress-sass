<script setup>
defineProps({
  activePath: {
    type: String,
    required: true,
  },
  moduleTitle: {
    type: String,
    required: true,
  },
  notice: {
    type: Object,
    required: true,
  },
  refreshing: {
    type: Boolean,
    default: false,
  },
  secondaryMenus: {
    type: Array,
    required: true,
  },
  user: {
    type: Object,
    default: null,
  },
})

defineEmits(['change-tab', 'refresh', 'logout'])
</script>

<template>
  <header class="header">
    <div class="header-left">
      <h2>{{ moduleTitle }}</h2>
      <div class="subnav">
        <button
          v-for="item in secondaryMenus"
          :key="item.key"
          class="subnav-item"
          :class="{ 'subnav-item-active': activePath === item.path }"
          @click="$emit('change-tab', item.path)"
        >
          {{ item.label }}
        </button>
      </div>
    </div>

    <div class="header-right">
      <div class="notice-banner" :class="`notice-${notice.type}`">{{ notice.message }}</div>
      <button class="button button-light" @click="$emit('refresh')" :disabled="refreshing">
        刷新
      </button>
      <div class="user-box">
        <div class="user-avatar">{{ (user?.nickname || user?.username)?.slice(0, 1)?.toUpperCase() }}</div>
        <div class="user-meta">
          <strong>{{ user?.nickname || user?.username }}</strong>
          <span>{{ user?.role }} / 租户 #{{ user?.tenantId }}</span>
        </div>
      </div>
      <button class="button button-ghost" @click="$emit('logout')">退出</button>
    </div>
  </header>
</template>
