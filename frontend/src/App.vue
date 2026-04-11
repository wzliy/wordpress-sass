<script setup>
import { ref, watch } from 'vue'
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
  modulePresentation,
  navigateModule,
  navigateTab,
  notice,
  pagePresentation,
  openUserEditor,
  previewSitePage,
  publishSitePage,
  ready,
  refreshing,
  rollbackSitePageVersion,
  saveSitePageDraft,
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

const sidebarOpen = ref(false)

watch(
  () => route.fullPath,
  () => {
    sidebarOpen.value = false
  }
)

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

function closeSidebar() {
  sidebarOpen.value = false
}
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

    <div
      v-else
      class="admin-layout"
      :class="{ 'admin-layout-sidebar-open': sidebarOpen }"
      :data-module="currentModule"
    >
      <button class="sidebar-overlay" type="button" @click="closeSidebar" />
      <SidebarNav
        :counts="counts"
        :current-module="currentModule"
        :module-presentation="modulePresentation"
        :user="user"
        @change-module="navigateModule"
        @close="closeSidebar"
      />

      <div class="content-shell">
        <HeaderBar
          :active-path="route.meta.activePath || route.path"
          :module-title="moduleTitle"
          :module-presentation="modulePresentation"
          :notice="notice"
          :page-presentation="pagePresentation"
          :refreshing="refreshing"
          :secondary-menus="secondaryMenus"
          :user="user"
          @change-tab="navigateTab"
          @logout="logout"
          @refresh="syncCurrentModule"
          @toggle-nav="toggleSidebar"
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
            @publish-page="publishSitePage"
            @rollback-page-version="rollbackSitePageVersion"
            @update-user="submitUpdateUser"
            @preview-page="previewSitePage"
            @save-page-draft="saveSitePageDraft"
          />
        </main>
      </div>
    </div>
  </RouterView>
</template>
