<script setup>
import { useRoute, useRouter, RouterView } from 'vue-router'
import LoadingScreen from './components/common/LoadingScreen.vue'
import HeaderBar from './components/layout/HeaderBar.vue'
import SidebarNav from './components/layout/SidebarNav.vue'
import { useConsoleApp } from './composables/useConsoleApp'

const route = useRoute()
const router = useRouter()

const {
  activeViewProps,
  counts,
  currentModule,
  handleDisableUser,
  handleEnableUser,
  handleTestConnection,
  login,
  logout,
  moduleTitle,
  navigateModule,
  navigateTab,
  notice,
  openUserEditor,
  ready,
  refreshing,
  secondaryMenus,
  submitChangePassword,
  submitCreateUser,
  submitPost,
  submitPublish,
  submitProvision,
  submitRegister,
  submitUpdateUser,
  syncCurrentModule,
  token,
  user,
} = useConsoleApp(route, router)
</script>

<template>
  <LoadingScreen v-if="!ready" />

  <RouterView v-else v-slot="{ Component, route: activeRoute }">
    <component
      :is="Component"
      v-if="activeRoute.meta.layout === 'auth'"
      v-bind="activeViewProps"
      @submit="login"
    />

    <div v-else class="admin-layout">
      <SidebarNav
        :counts="counts"
        :current-module="currentModule"
        @change-module="navigateModule"
      />

      <div class="content-shell">
        <HeaderBar
          :active-path="route.meta.activePath || route.path"
          :module-title="moduleTitle"
          :notice="notice"
          :refreshing="refreshing"
          :secondary-menus="secondaryMenus"
          :user="user"
          @change-tab="navigateTab"
          @logout="logout"
          @refresh="syncCurrentModule"
        />

        <main class="content">
          <component
            :is="Component"
            v-bind="activeViewProps"
            @change-password="submitChangePassword"
            @create-post="submitPost"
            @create-user="submitCreateUser"
            @edit-user="openUserEditor"
            @submit="submitPublish"
            @register-site="submitRegister"
            @retry="syncCurrentModule"
            @disable-user="handleDisableUser"
            @enable-user="handleEnableUser"
            @test-site="handleTestConnection"
            @provision-site="submitProvision"
            @update-user="submitUpdateUser"
          />
        </main>
      </div>
    </div>
  </RouterView>
</template>
