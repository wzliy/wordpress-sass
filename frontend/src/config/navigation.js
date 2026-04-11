export const MODULE_ITEMS = [
  {
    key: 'dashboard',
    label: '总览',
    defaultPath: '/dashboard',
    glyph: '01',
    eyebrow: 'Mission Control',
    description: '集中查看站点、内容与发布健康度。',
    countKey: null,
  },
  {
    key: 'site',
    label: '站点管理',
    defaultPath: '/sites/list',
    glyph: '02',
    eyebrow: 'Site Matrix',
    description: '管理站点、模板、域名与建站动作。',
    countKey: 'site',
  },
  {
    key: 'post',
    label: '文章管理',
    defaultPath: '/posts/list',
    glyph: '03',
    eyebrow: 'Content Stream',
    description: '维护内容资产与草稿生产流。',
    countKey: 'post',
  },
  {
    key: 'publish',
    label: '发布中心',
    defaultPath: '/publish/create',
    glyph: '04',
    eyebrow: 'Release Queue',
    description: '集中编排发布任务和历史记录。',
    countKey: null,
  },
  {
    key: 'user',
    label: '用户管理',
    defaultPath: '/users/list',
    glyph: '05',
    eyebrow: 'Access Layer',
    description: '管理后台成员、租户和权限触点。',
    countKey: 'user',
  },
]

export const SUMMARY_ITEMS = MODULE_ITEMS.filter((item) => ['site', 'post', 'user'].includes(item.key))

export const MODULE_TITLES = {
  dashboard: '总览',
  site: '站点管理',
  post: '文章管理',
  publish: '发布中心',
  user: '用户管理',
}

export const MODULE_PRESENTATIONS = {
  dashboard: {
    eyebrow: 'Mission Control',
    title: '运营总览',
    description: '用一块深色调度台统一观察站点、内容、成员和发布流的健康度。',
    accentLabel: 'Cross-border Console',
  },
  site: {
    eyebrow: 'Site Matrix',
    title: '站点中枢',
    description: '围绕站点、域名、模板和建站动作建立高频运营入口。',
    accentLabel: 'Storefront Ops',
  },
  post: {
    eyebrow: 'Content Stream',
    title: '内容资产',
    description: '以更轻的卡片和表单形态组织文章草稿、内容录入和后续分发。',
    accentLabel: 'Editorial Layer',
  },
  publish: {
    eyebrow: 'Release Queue',
    title: '发布中心',
    description: '将站点选择、任务提交和历史记录收敛到单条发布轨道。',
    accentLabel: 'Release Engine',
  },
  user: {
    eyebrow: 'Access Layer',
    title: '成员与权限',
    description: '把后台账号、租户归属和启停控制放到统一的权限界面。',
    accentLabel: 'Identity Control',
  },
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

export const PAGE_PRESENTATIONS = {
  login: {
    eyebrow: 'Control Access',
    title: '进入暗色控制台',
    description: '以更聚焦的方式进入站点、发布和运营链路。',
  },
  'dashboard-home': {
    eyebrow: 'Mission Control',
    title: '运营总览',
    description: '用核心指标、趋势和最近动态快速判断今天应该先处理什么。',
  },
  'site-list': {
    eyebrow: 'Site Matrix',
    title: '站点舰桥',
    description: '按域名、接入方式和运行状态管理站点资产，并快速进入工作台。',
  },
  'site-register': {
    eyebrow: 'Connect Existing Site',
    title: '接入已有站点',
    description: '将外部 WordPress 站点纳入统一控制台，补齐连接、域名和运营入口。',
  },
  'site-provision': {
    eyebrow: 'Launch Workflow',
    title: '自动建站',
    description: '先选模板，再提交最少字段，让系统自动完成基础站点初始化。',
  },
  'site-workspace': {
    eyebrow: 'Site Operations',
    title: '站点工作台',
    description: '把准备度、模块状态和快捷动作收口到单站点操作视图。',
  },
  'site-page-editor': {
    eyebrow: 'Page Builder',
    title: '首页编辑器',
    description: '围绕首页结构、菜单和精选商品做草稿、预览和发布。',
  },
  'post-list': {
    eyebrow: 'Content Stream',
    title: '文章列表',
    description: '以内容卡片方式快速定位草稿和最近文章。',
  },
  'post-create': {
    eyebrow: 'Compose Draft',
    title: '新建文章',
    description: '在更聚焦的单栏表单里录入标题、正文和待发布内容。',
  },
  'publish-create': {
    eyebrow: 'Release Queue',
    title: '发起发布任务',
    description: '选择目标文章和站点，形成清晰的发布批次。',
  },
  'publish-history': {
    eyebrow: 'Delivery Log',
    title: '发布记录',
    description: '复盘发布结果、失败原因和历史轨迹。',
  },
  'user-list': {
    eyebrow: 'Access Layer',
    title: '用户列表',
    description: '按状态、角色和租户维度管理后台成员。',
  },
  'user-create': {
    eyebrow: 'Provision Access',
    title: '新增用户',
    description: '以更明确的权限语义创建新的后台管理账号。',
  },
  'user-edit': {
    eyebrow: 'Profile Tuning',
    title: '编辑用户资料',
    description: '维护成员昵称、邮箱和归属信息。',
  },
  'user-password': {
    eyebrow: 'Security Reset',
    title: '修改密码',
    description: '在独立安全面板中更新当前账号的登录凭据。',
  },
}

export function moduleDefaultPath(moduleKey) {
  return MODULE_ITEMS.find((item) => item.key === moduleKey)?.defaultPath || '/dashboard'
}
