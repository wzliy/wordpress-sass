<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: '',
  },
  tone: {
    type: String,
    default: 'empty',
  },
  actionLabel: {
    type: String,
    default: '',
  },
})

defineEmits(['action'])
</script>

<template>
  <div class="state-panel" :class="`state-panel-${tone}`">
    <div class="state-panel-badge" />
    <div class="state-panel-content">
      <strong>{{ title }}</strong>
      <p v-if="description">{{ description }}</p>
    </div>
    <button
      v-if="actionLabel"
      class="button button-light"
      type="button"
      @click="$emit('action')"
    >
      {{ actionLabel }}
    </button>
  </div>
</template>
