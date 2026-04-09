export const MODULE_ITEMS = [
  { key: 'dashboard', label: '总览', defaultPath: '/dashboard' },
  { key: 'site', label: '站点管理', defaultPath: '/sites/list' },
  { key: 'post', label: '文章管理', defaultPath: '/posts/list' },
  { key: 'publish', label: '发布中心', defaultPath: '/publish/create' },
  { key: 'user', label: '用户管理', defaultPath: '/users/list' },
]

export const SUMMARY_ITEMS = MODULE_ITEMS.filter((item) => ['site', 'post', 'user'].includes(item.key))

export const MODULE_TITLES = {
  dashboard: '总览',
  site: '站点管理',
  post: '文章管理',
  publish: '发布中心',
  user: '用户管理',
}

export const SECONDARY_MENUS = {
  dashboard: [
    { key: 'dashboard-home', label: '概览', path: '/dashboard' },
  ],
  site: [
    { key: 'site-list', label: '站点列表', path: '/sites/list' },
    { key: 'site-register', label: '接入站点', path: '/sites/register' },
    { key: 'site-provision', label: '自动建站', path: '/sites/provision' },
  ],
  post: [
    { key: 'post-list', label: '文章列表', path: '/posts/list' },
    { key: 'post-create', label: '新建文章', path: '/posts/create' },
  ],
  publish: [
    { key: 'publish-create', label: '发布任务', path: '/publish/create' },
    { key: 'publish-history', label: '发布记录', path: '/publish/history' },
  ],
  user: [
    { key: 'user-list', label: '用户列表', path: '/users/list' },
    { key: 'user-create', label: '新增用户', path: '/users/create' },
    { key: 'user-edit', label: '编辑资料', path: '/users/edit' },
    { key: 'user-password', label: '修改密码', path: '/users/password' },
  ],
}

export function moduleDefaultPath(moduleKey) {
  return MODULE_ITEMS.find((item) => item.key === moduleKey)?.defaultPath || '/dashboard'
}
