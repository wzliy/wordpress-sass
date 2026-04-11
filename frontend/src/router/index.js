import { createRouter, createWebHistory } from 'vue-router'
import LoginPage from '../components/auth/LoginPage.vue'
import DashboardPage from '../pages/dashboard/DashboardPage.vue'
import PostCreatePage from '../pages/post/PostCreatePage.vue'
import PostListPage from '../pages/post/PostListPage.vue'
import PublishCreatePage from '../pages/publish/PublishCreatePage.vue'
import PublishHistoryPage from '../pages/publish/PublishHistoryPage.vue'
import SiteListPage from '../pages/site/SiteListPage.vue'
import SitePageEditorPage from '../pages/site/SitePageEditorPage.vue'
import SiteProvisionPage from '../pages/site/SiteProvisionPage.vue'
import SiteRegisterPage from '../pages/site/SiteRegisterPage.vue'
import SiteWorkspacePage from '../pages/site/SiteWorkspacePage.vue'
import UserCreatePage from '../pages/user/UserCreatePage.vue'
import UserEditPage from '../pages/user/UserEditPage.vue'
import UserListPage from '../pages/user/UserListPage.vue'
import UserPasswordPage from '../pages/user/UserPasswordPage.vue'

const TOKEN_KEY = 'wpss_token'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard',
    },
    {
      path: '/dashboard',
      name: 'dashboard-home',
      component: DashboardPage,
      meta: {
        layout: 'admin',
        module: 'dashboard',
      },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginPage,
      meta: {
        layout: 'auth',
      },
    },
    {
      path: '/sites/list',
      name: 'site-list',
      component: SiteListPage,
      meta: {
        layout: 'admin',
        module: 'site',
      },
    },
    {
      path: '/sites/register',
      name: 'site-register',
      component: SiteRegisterPage,
      meta: {
        layout: 'admin',
        module: 'site',
      },
    },
    {
      path: '/sites/provision',
      name: 'site-provision',
      component: SiteProvisionPage,
      meta: {
        layout: 'admin',
        module: 'site',
      },
    },
    {
      path: '/sites/:id/workspace',
      name: 'site-workspace',
      component: SiteWorkspacePage,
      meta: {
        layout: 'admin',
        module: 'site',
        activePath: '/sites/list',
      },
    },
    {
      path: '/sites/:id/pages/:pageKey/editor',
      name: 'site-page-editor',
      component: SitePageEditorPage,
      meta: {
        layout: 'admin',
        module: 'site',
        activePath: '/sites/list',
      },
    },
    {
      path: '/posts/list',
      name: 'post-list',
      component: PostListPage,
      meta: {
        layout: 'admin',
        module: 'post',
      },
    },
    {
      path: '/posts/create',
      name: 'post-create',
      component: PostCreatePage,
      meta: {
        layout: 'admin',
        module: 'post',
      },
    },
    {
      path: '/publish/create',
      name: 'publish-create',
      component: PublishCreatePage,
      meta: {
        layout: 'admin',
        module: 'publish',
      },
    },
    {
      path: '/publish/history',
      name: 'publish-history',
      component: PublishHistoryPage,
      meta: {
        layout: 'admin',
        module: 'publish',
      },
    },
    {
      path: '/users/list',
      name: 'user-list',
      component: UserListPage,
      meta: {
        layout: 'admin',
        module: 'user',
      },
    },
    {
      path: '/users/create',
      name: 'user-create',
      component: UserCreatePage,
      meta: {
        layout: 'admin',
        module: 'user',
      },
    },
    {
      path: '/users/edit/:id',
      name: 'user-edit',
      component: UserEditPage,
      meta: {
        layout: 'admin',
        module: 'user',
        activePath: '/users/list',
      },
    },
    {
      path: '/users/password',
      name: 'user-password',
      component: UserPasswordPage,
      meta: {
        layout: 'admin',
        module: 'user',
      },
    },
  ],
})

router.beforeEach((to) => {
  const hasToken = Boolean(localStorage.getItem(TOKEN_KEY))

  if (to.meta.layout === 'admin' && !hasToken) {
    return '/login'
  }

  if (to.name === 'login' && hasToken) {
    return '/dashboard'
  }

  return true
})

export default router
