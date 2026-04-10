import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { currentUser as fetchCurrentUser, login as loginRequest } from '../api/auth'
import { AUTH_EXPIRED_EVENT, TOKEN_KEY, USER_KEY } from '../api/http'
import { createPost, listPosts } from '../api/post'
import { listPublishRecords, publishPost as publishPostRequest } from '../api/publish'
import { getSiteWorkspace, listSiteTemplates, listSites, provisionSite, registerSite, testSiteConnection } from '../api/site'
import { changePassword, createUser, disableUser, enableUser, getUserDetail, listUsers, updateUserProfile } from '../api/user'
import { MODULE_PRESENTATIONS, MODULE_TITLES, PAGE_PRESENTATIONS, SECONDARY_MENUS, moduleDefaultPath } from '../config/navigation'
import {
  validateChangePasswordForm,
  validateCreateUserForm,
  validateLoginForm,
  validatePostForm,
  validatePublishForm,
  validateProvisionSiteForm,
  validateRegisterSiteForm,
  validateUpdateUserForm,
} from '../utils/validators'

const HOME_PATH = '/dashboard'

export function useConsoleApp(route, router) {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(readStoredUser())
  const ready = ref(false)

  const sites = ref([])
  const posts = ref([])
  const users = ref([])
  const siteWorkspace = ref(null)
  const siteTemplates = ref([])

  const loginForm = reactive({
    username: 'admin',
    password: 'admin123',
  })

  const registerForm = reactive({
    name: '',
    baseUrl: '',
    wpUsername: '',
    appPassword: '',
  })

  const provisionForm = reactive({
    templateCode: '',
    name: '',
    adminEmail: '',
    countryCode: 'US',
    languageCode: 'en',
    currencyCode: 'USD',
    subdomainPrefix: '',
  })

  const postForm = reactive({
    title: '',
    content: '',
  })

  const publishForm = reactive({
    postId: '',
    siteIds: [],
  })

  const createUserForm = reactive({
    username: '',
    password: '',
    email: '',
  })

  const editUserForm = reactive({
    userId: '',
    username: '',
    email: '',
    nickname: '',
  })

  const passwordForm = reactive({
    currentPassword: '',
    newPassword: '',
  })

  const loading = reactive({
    auth: false,
    sites: false,
    posts: false,
    users: false,
    register: false,
    provision: false,
    createPost: false,
    publish: false,
    publishRecords: false,
    siteWorkspace: false,
    siteTemplates: false,
    createUser: false,
    updateUser: false,
    changePassword: false,
    userDetail: false,
    userActionId: null,
    testId: null,
  })

  const errors = reactive({
    sites: '',
    posts: '',
    publishRecords: '',
    users: '',
    siteWorkspace: '',
    siteTemplates: '',
  })

  const formErrors = reactive({
    login: {
      username: '',
      password: '',
      form: '',
    },
    register: {
      name: '',
      baseUrl: '',
      wpUsername: '',
      appPassword: '',
      form: '',
    },
    provision: {
      templateCode: '',
      name: '',
      adminEmail: '',
      countryCode: '',
      languageCode: '',
      currencyCode: '',
      subdomainPrefix: '',
      form: '',
    },
    post: {
      title: '',
      content: '',
      form: '',
    },
    publish: {
      postId: '',
      siteIds: '',
      form: '',
    },
    createUser: {
      username: '',
      email: '',
      password: '',
      form: '',
    },
    changePassword: {
      currentPassword: '',
      newPassword: '',
      form: '',
    },
    editUser: {
      email: '',
      nickname: '',
      form: '',
    },
  })

  const notice = reactive({
    type: 'info',
    message: '就绪',
  })
  const publishResults = ref([])
  const publishRecords = ref([])

  const siteCount = computed(() => sites.value.length)
  const activeSiteCount = computed(() => sites.value.filter((site) => site.status === 1).length)
  const postCount = computed(() => posts.value.length)
  const draftCount = computed(() => posts.value.filter((post) => post.status === 'DRAFT').length)
  const userCount = computed(() => users.value.length)
  const activeUserCount = computed(() => users.value.filter((account) => account.status === 'ACTIVE').length)
  const disabledUserCount = computed(() => users.value.filter((account) => account.status === 'DISABLED').length)
  const counts = computed(() => ({
    site: siteCount.value,
    post: postCount.value,
    user: userCount.value,
  }))
  const currentModule = computed(() => route.meta.module || 'dashboard')
  const moduleTitle = computed(() => MODULE_TITLES[currentModule.value] || '管理后台')
  const modulePresentation = computed(() => MODULE_PRESENTATIONS[currentModule.value] || MODULE_PRESENTATIONS.dashboard)
  const pagePresentation = computed(() => PAGE_PRESENTATIONS[route.name] || modulePresentation.value)
  const secondaryMenus = computed(() => SECONDARY_MENUS[currentModule.value] || [])
  const refreshing = computed(() => loading.sites || loading.posts || loading.users)
  const activeViewProps = computed(() => {
    if (route.name === 'dashboard-home') {
      return {
        sites: sites.value,
        posts: posts.value,
        users: users.value,
        publishRecords: publishRecords.value,
        activeSiteCount: activeSiteCount.value,
        draftCount: draftCount.value,
        loading: refreshing.value,
      }
    }

    if (route.name === 'site-list') {
      return {
        sites: sites.value,
        loading: loading.sites,
        activeSiteCount: activeSiteCount.value,
        siteCount: siteCount.value,
        testId: loading.testId,
        errorMessage: errors.sites,
      }
    }

    if (route.name === 'site-workspace') {
      return {
        workspace: siteWorkspace.value,
        loading: loading.siteWorkspace,
        errorMessage: errors.siteWorkspace,
      }
    }

    if (route.name === 'site-register') {
      return {
        form: registerForm,
        errors: formErrors.register,
        loading: loading.register,
      }
    }

    if (route.name === 'site-provision') {
      return {
        form: provisionForm,
        errors: formErrors.provision,
        loading: loading.provision,
        templates: siteTemplates.value,
        templateLoading: loading.siteTemplates,
        templateError: errors.siteTemplates,
      }
    }

    if (route.name === 'post-list') {
      return {
        posts: posts.value,
        loading: loading.posts,
        draftCount: draftCount.value,
        postCount: postCount.value,
        errorMessage: errors.posts,
      }
    }

    if (route.name === 'post-create') {
      return {
        form: postForm,
        errors: formErrors.post,
        loading: loading.createPost,
      }
    }

    if (route.name === 'publish-create') {
      return {
        form: publishForm,
        errors: formErrors.publish,
        loading: loading.publish,
        posts: posts.value,
        results: publishResults.value,
        sites: sites.value,
      }
    }

    if (route.name === 'publish-history') {
      return {
        records: publishRecords.value,
        loading: loading.publishRecords,
        errorMessage: errors.publishRecords,
      }
    }

    if (route.name === 'user-list') {
      return {
        loading: loading.users,
        userCount: userCount.value,
        activeUserCount: activeUserCount.value,
        disabledUserCount: disabledUserCount.value,
        actionUserId: loading.userActionId,
        users: users.value,
        errorMessage: errors.users,
      }
    }

    if (route.name === 'user-create') {
      return {
        form: createUserForm,
        errors: formErrors.createUser,
        loading: loading.createUser,
      }
    }

    if (route.name === 'user-edit') {
      return {
        form: editUserForm,
        errors: formErrors.editUser,
        loading: loading.userDetail || loading.updateUser,
      }
    }

    if (route.name === 'user-password') {
      return {
        form: passwordForm,
        errors: formErrors.changePassword,
        loading: loading.changePassword,
      }
    }

    return {
      form: loginForm,
      errors: formErrors.login,
      loading: loading.auth,
      notice,
    }
  })

  function setNotice(type, message) {
    notice.type = type
    notice.message = message
  }

  function clearErrorBag(errorBag) {
    Object.keys(errorBag).forEach((key) => {
      errorBag[key] = ''
    })
  }

  function applyValidation(errorBag, nextErrors) {
    clearErrorBag(errorBag)
    Object.entries(nextErrors).forEach(([key, value]) => {
      errorBag[key] = value
    })
    return Object.keys(nextErrors).length === 0
  }

  function saveSession(authResult) {
    token.value = authResult.token || token.value
    user.value = {
      userId: authResult.userId,
      tenantId: authResult.tenantId,
      username: authResult.username,
      email: authResult.email,
      nickname: authResult.nickname || authResult.username,
      role: authResult.role,
      expiresAt: authResult.expiresAt || null,
      expireSeconds: authResult.expireSeconds || null,
    }
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
  }

  function clearSession() {
    token.value = ''
    user.value = null
    sites.value = []
    posts.value = []
    users.value = []
    siteWorkspace.value = null
    siteTemplates.value = []
    provisionForm.templateCode = ''
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  async function handleAuthExpired(event) {
    clearSession()
    setNotice('error', event.detail?.message || '登录已失效，请重新登录')
    if (route.name !== 'login') {
      await router.replace('/login')
    }
  }

  function navigateModule(moduleKey) {
    router.push(moduleDefaultPath(moduleKey))
  }

  function navigateTab(path) {
    if (route.path !== path) {
      router.push(path)
    }
  }

  async function login() {
    if (!applyValidation(formErrors.login, validateLoginForm(loginForm))) {
      formErrors.login.form = '请先修正表单中的输入问题'
      return
    }

    loading.auth = true
    try {
      const result = await loginRequest({ ...loginForm })
      saveSession(result)
      clearErrorBag(formErrors.login)
      setNotice('success', '登录成功')
      await Promise.all([loadSites(), loadPosts(), loadPublishRecords(), loadUsers()])
      await router.replace(HOME_PATH)
    } catch (error) {
      formErrors.login.form = error.message
      setNotice('error', error.message)
    } finally {
      loading.auth = false
      ready.value = true
    }
  }

  async function restoreSession() {
    if (!token.value) {
      ready.value = true
      return
    }

    loading.auth = true
    try {
      const result = await fetchCurrentUser()
      saveSession({ ...result, token: token.value })
      await Promise.all([loadSites(), loadPosts(), loadPublishRecords(), loadUsers()])
      if (route.name === 'login') {
        await router.replace(HOME_PATH)
      }
    } catch (error) {
      if (!error?.isAuthError) {
        clearSession()
        setNotice('error', '登录已失效，请重新登录')
        await router.replace('/login')
      }
    } finally {
      loading.auth = false
      ready.value = true
    }
  }

  function logout() {
    clearSession()
    setNotice('info', '已退出登录')
    router.replace('/login')
  }

  async function loadSites() {
    if (!token.value) return
    loading.sites = true
    try {
      sites.value = await listSites()
      errors.sites = ''
    } catch (error) {
      errors.sites = error.message
      setNotice('error', error.message)
    } finally {
      loading.sites = false
    }
  }

  async function loadSiteWorkspace(siteId) {
    if (!token.value || !siteId) return
    loading.siteWorkspace = true
    try {
      siteWorkspace.value = await getSiteWorkspace(Number(siteId))
      errors.siteWorkspace = ''
    } catch (error) {
      siteWorkspace.value = null
      errors.siteWorkspace = error.message
      setNotice('error', error.message)
    } finally {
      loading.siteWorkspace = false
    }
  }

  async function loadPosts() {
    if (!token.value) return
    loading.posts = true
    try {
      posts.value = await listPosts()
      errors.posts = ''
    } catch (error) {
      errors.posts = error.message
      setNotice('error', error.message)
    } finally {
      loading.posts = false
    }
  }

  async function loadUsers() {
    if (!token.value) return
    loading.users = true
    try {
      users.value = await listUsers()
      errors.users = ''
    } catch (error) {
      errors.users = error.message
      setNotice('error', error.message)
    } finally {
      loading.users = false
    }
  }

  async function loadUserDetail(userId) {
    if (!token.value || !userId) return
    loading.userDetail = true
    try {
      const detail = await getUserDetail(userId)
      editUserForm.userId = detail.id
      editUserForm.username = detail.username || ''
      editUserForm.email = detail.email || ''
      editUserForm.nickname = detail.nickname || detail.username || ''
      clearErrorBag(formErrors.editUser)
    } catch (error) {
      formErrors.editUser.form = error.message
      setNotice('error', error.message)
    } finally {
      loading.userDetail = false
    }
  }

  async function loadPublishRecords() {
    if (!token.value) return
    loading.publishRecords = true
    try {
      publishRecords.value = await listPublishRecords()
      errors.publishRecords = ''
    } catch (error) {
      errors.publishRecords = error.message
      setNotice('error', error.message)
    } finally {
      loading.publishRecords = false
    }
  }

  async function loadSiteTemplates() {
    if (!token.value) return
    loading.siteTemplates = true
    try {
      siteTemplates.value = await listSiteTemplates()
      const selectedStillExists = siteTemplates.value.some((template) => template.code === provisionForm.templateCode)
      if ((!provisionForm.templateCode || !selectedStillExists) && siteTemplates.value.length > 0) {
        provisionForm.templateCode = siteTemplates.value[0].code
      }
      errors.siteTemplates = ''
    } catch (error) {
      errors.siteTemplates = error.message
      setNotice('error', error.message)
    } finally {
      loading.siteTemplates = false
    }
  }

  async function syncCurrentModule() {
    if (currentModule.value === 'dashboard') {
      await Promise.all([loadSites(), loadPosts(), loadPublishRecords(), loadUsers()])
    }
    if (currentModule.value === 'site') {
      if (route.name === 'site-workspace' && route.params.id) {
        await Promise.all([loadSites(), loadSiteWorkspace(route.params.id), loadSiteTemplates()])
      } else {
        await Promise.all([loadSites(), loadSiteTemplates()])
      }
    }
    if (currentModule.value === 'post') await loadPosts()
    if (currentModule.value === 'publish') await Promise.all([loadPosts(), loadSites(), loadPublishRecords()])
    if (currentModule.value === 'user') await loadUsers()
    setNotice('success', '数据已刷新')
  }

  async function submitRegister() {
    if (!applyValidation(formErrors.register, validateRegisterSiteForm(registerForm))) {
      formErrors.register.form = '请先修正表单中的输入问题'
      return
    }

    loading.register = true
    try {
      const site = await registerSite({ ...registerForm })
      registerForm.name = ''
      registerForm.baseUrl = ''
      registerForm.wpUsername = ''
      registerForm.appPassword = ''
      clearErrorBag(formErrors.register)
      setNotice('success', '站点已创建')
      await loadSites()
      await router.push(`/sites/${site.id}/workspace`)
    } catch (error) {
      formErrors.register.form = error.message
      setNotice('error', error.message)
    } finally {
      loading.register = false
    }
  }

  async function submitProvision() {
    if (!applyValidation(formErrors.provision, validateProvisionSiteForm(provisionForm))) {
      formErrors.provision.form = '请先修正表单中的输入问题'
      return
    }

    loading.provision = true
    try {
      const result = await provisionSite({ ...provisionForm })
      provisionForm.name = ''
      provisionForm.adminEmail = ''
      provisionForm.countryCode = 'US'
      provisionForm.languageCode = 'en'
      provisionForm.currencyCode = 'USD'
      provisionForm.subdomainPrefix = ''
      clearErrorBag(formErrors.provision)
      setNotice('success', '建站完成，已进入工作台')
      await loadSites()
      await router.push(`/sites/${result.siteId}/workspace`)
    } catch (error) {
      formErrors.provision.form = error.message
      setNotice('error', error.message)
    } finally {
      loading.provision = false
    }
  }

  async function submitPost() {
    if (!applyValidation(formErrors.post, validatePostForm(postForm))) {
      formErrors.post.form = '请先修正表单中的输入问题'
      return
    }

    loading.createPost = true
    try {
      await createPost({ ...postForm })
      postForm.title = ''
      postForm.content = ''
      clearErrorBag(formErrors.post)
      setNotice('success', '文章已创建')
      await loadPosts()
      await router.push('/posts/list')
    } catch (error) {
      formErrors.post.form = error.message
      setNotice('error', error.message)
    } finally {
      loading.createPost = false
    }
  }

  async function submitPublish() {
    if (!applyValidation(formErrors.publish, validatePublishForm(publishForm))) {
      formErrors.publish.form = '请先修正表单中的输入问题'
      return
    }

    loading.publish = true
    try {
      const result = await publishPostRequest({
        postId: Number(publishForm.postId),
        siteIds: publishForm.siteIds.map((siteId) => Number(siteId)),
      })
      publishResults.value = result.results
      await loadPublishRecords()
      clearErrorBag(formErrors.publish)
      setNotice('success', '发布任务已执行')
    } catch (error) {
      formErrors.publish.form = error.message
      setNotice('error', error.message)
    } finally {
      loading.publish = false
    }
  }

  async function submitCreateUser() {
    if (!applyValidation(formErrors.createUser, validateCreateUserForm(createUserForm))) {
      formErrors.createUser.form = '请先修正表单中的输入问题'
      return
    }

    loading.createUser = true
    try {
      await createUser({ ...createUserForm })
      createUserForm.username = ''
      createUserForm.password = ''
      createUserForm.email = ''
      clearErrorBag(formErrors.createUser)
      setNotice('success', '用户已创建')
      await loadUsers()
      await router.push('/users/list')
    } catch (error) {
      formErrors.createUser.form = error.message
      setNotice('error', error.message)
    } finally {
      loading.createUser = false
    }
  }

  async function submitUpdateUser() {
    if (!applyValidation(formErrors.editUser, validateUpdateUserForm(editUserForm))) {
      formErrors.editUser.form = '请先修正表单中的输入问题'
      return
    }

    loading.updateUser = true
    try {
      const updated = await updateUserProfile({
        userId: Number(editUserForm.userId),
        email: editUserForm.email,
        nickname: editUserForm.nickname,
      })
      clearErrorBag(formErrors.editUser)
      if (Number(updated.id) === Number(user.value?.userId)) {
        user.value = {
          ...user.value,
          email: updated.email,
          nickname: updated.nickname,
        }
        localStorage.setItem(USER_KEY, JSON.stringify(user.value))
      }
      setNotice('success', '用户资料已更新')
      await loadUsers()
      await router.push('/users/list')
    } catch (error) {
      formErrors.editUser.form = error.message
      setNotice('error', error.message)
    } finally {
      loading.updateUser = false
    }
  }

  async function submitChangePassword() {
    if (!applyValidation(formErrors.changePassword, validateChangePasswordForm(passwordForm))) {
      formErrors.changePassword.form = '请先修正表单中的输入问题'
      return
    }

    loading.changePassword = true
    try {
      await changePassword({ ...passwordForm })
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      clearErrorBag(formErrors.changePassword)
      setNotice('success', '密码已更新')
      await router.push('/users/list')
    } catch (error) {
      formErrors.changePassword.form = error.message
      setNotice('error', error.message)
    } finally {
      loading.changePassword = false
    }
  }

  async function handleDisableUser(account) {
    loading.userActionId = account.id
    try {
      await disableUser(account.id)
      await loadUsers()
      setNotice('success', `用户 ${account.username} 已禁用`)
    } catch (error) {
      setNotice('error', error.message)
    } finally {
      loading.userActionId = null
    }
  }

  async function handleEnableUser(account) {
    loading.userActionId = account.id
    try {
      await enableUser(account.id)
      await loadUsers()
      setNotice('success', `用户 ${account.username} 已启用`)
    } catch (error) {
      setNotice('error', error.message)
    } finally {
      loading.userActionId = null
    }
  }

  async function openUserEditor(account) {
    editUserForm.userId = account.id
    editUserForm.username = account.username || ''
    editUserForm.email = account.email || ''
    editUserForm.nickname = account.nickname || account.username || ''
    clearErrorBag(formErrors.editUser)
    await router.push(`/users/edit/${account.id}`)
  }

  async function handleTestConnection(site) {
    loading.testId = site.id
    try {
      const result = await testSiteConnection(site.id)
      setNotice('success', result.message)
      await loadSites()
    } catch (error) {
      setNotice('error', error.message)
    } finally {
      loading.testId = null
    }
  }

  onMounted(() => {
    window.addEventListener(AUTH_EXPIRED_EVENT, handleAuthExpired)
    restoreSession()
  })

  watch(
    () => [route.name, route.params.id, token.value],
    async ([routeName, nextId, nextToken]) => {
      if (routeName === 'user-edit' && nextId && nextToken) {
        await loadUserDetail(nextId)
      }
      if (routeName === 'site-workspace' && nextId && nextToken) {
        await loadSiteWorkspace(nextId)
      }
      if (routeName === 'site-provision' && nextToken) {
        await loadSiteTemplates()
      }
    },
    { immediate: true },
  )

  onUnmounted(() => {
    window.removeEventListener(AUTH_EXPIRED_EVENT, handleAuthExpired)
  })

  return {
    activeViewProps,
    counts,
    currentModule,
    handleTestConnection,
    handleDisableUser,
    handleEnableUser,
    openUserEditor,
    loadPosts,
    loadPublishRecords,
    loadSites,
    loadSiteWorkspace,
    loadSiteTemplates,
    loadUsers,
    login,
    logout,
    modulePresentation,
    moduleTitle,
    navigateModule,
    navigateTab,
    notice,
    pagePresentation,
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
  }
}

function readStoredUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null

  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}
