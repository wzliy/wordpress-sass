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
  modulePresentation: {
    type: Object,
    required: true,
  },
  notice: {
    type: Object,
    required: true,
  },
  pagePresentation: {
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

defineEmits(['change-tab', 'refresh', 'logout', 'toggle-nav'])
</script>

<template>
  <header class="header">
    <div class="header-utility">
      <div class="header-utility-left">
        <button class="nav-toggle button button-ghost" type="button" @click="$emit('toggle-nav')">
          导航
        </button>
        <div class="header-module-chip">
          <span>{{ modulePresentation.eyebrow }}</span>
          <strong>{{ modulePresentation.accentLabel }}</strong>
        </div>
      </div>

      <div class="header-right">
        <div class="notice-banner" :class="`notice-${notice.type}`">{{ notice.message }}</div>
        <button class="button button-light" @click="$emit('refresh')" :disabled="refreshing">
          {{ refreshing ? '同步中' : '刷新' }}
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
    </div>

    <div class="header-stage">
      <div class="header-left">
        <span class="header-kicker">{{ pagePresentation.eyebrow }}</span>
        <h2>{{ pagePresentation.title || moduleTitle }}</h2>
        <p>{{ pagePresentation.description || modulePresentation.description }}</p>
        <div class="subnav" v-if="secondaryMenus.length > 0">
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

      <aside class="header-stage-card">
        <span>Module Lens</span>
        <strong>{{ moduleTitle }}</strong>
        <p>{{ modulePresentation.description }}</p>
      </aside>
    </div>
  </header>
</template>
