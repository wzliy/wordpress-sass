const USERNAME_REGEX = /^[a-zA-Z0-9_-]{3,32}$/
const SUBDOMAIN_REGEX = /^[a-z0-9-]{3,32}$/

function isBlank(value) {
  return !value || !String(value).trim()
}

function isValidUrl(value) {
  try {
    const url = new URL(value)
    return ['http:', 'https:'].includes(url.protocol)
  } catch {
    return false
  }
}

function isValidEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
}

export function validateLoginForm(form) {
  const errors = {}

  if (isBlank(form.username)) {
    errors.username = '请输入用户名'
  }

  if (isBlank(form.password)) {
    errors.password = '请输入密码'
  } else if (String(form.password).length < 6) {
    errors.password = '密码长度不能少于 6 位'
  }

  return errors
}

export function validateRegisterSiteForm(form) {
  const errors = {}

  if (isBlank(form.name)) {
    errors.name = '请输入站点名称'
  } else if (String(form.name).trim().length < 2) {
    errors.name = '站点名称至少 2 个字符'
  }

  if (isBlank(form.baseUrl)) {
    errors.baseUrl = '请输入站点地址'
  } else if (!isValidUrl(String(form.baseUrl).trim())) {
    errors.baseUrl = '请输入有效的站点地址，需包含 http:// 或 https://'
  }

  if (isBlank(form.wpUsername)) {
    errors.wpUsername = '请输入 WordPress 用户名'
  } else if (!USERNAME_REGEX.test(String(form.wpUsername).trim())) {
    errors.wpUsername = '用户名需为 3-32 位字母、数字、下划线或中划线'
  }

  if (isBlank(form.appPassword)) {
    errors.appPassword = '请输入应用密码'
  } else if (String(form.appPassword).trim().length < 8) {
    errors.appPassword = '应用密码长度不能少于 8 位'
  }

  return errors
}

export function validateProvisionSiteForm(form) {
  const errors = {}

  if (isBlank(form.templateCode)) {
    errors.templateCode = '请选择站点模板'
  }

  if (isBlank(form.name)) {
    errors.name = '请输入站点名称'
  } else if (String(form.name).trim().length < 2) {
    errors.name = '站点名称至少 2 个字符'
  }

  if (isBlank(form.adminEmail)) {
    errors.adminEmail = '请输入管理员邮箱'
  } else if (!isValidEmail(String(form.adminEmail).trim())) {
    errors.adminEmail = '请输入有效的邮箱地址'
  }

  if (isBlank(form.countryCode)) {
    errors.countryCode = '请选择国家'
  }

  if (isBlank(form.languageCode)) {
    errors.languageCode = '请选择语言'
  }

  if (isBlank(form.currencyCode)) {
    errors.currencyCode = '请选择币种'
  }

  if (isBlank(form.subdomainPrefix)) {
    errors.subdomainPrefix = '请输入子域名前缀'
  } else if (!SUBDOMAIN_REGEX.test(String(form.subdomainPrefix).trim())) {
    errors.subdomainPrefix = '子域名前缀需为 3-32 位小写字母、数字或中划线'
  }

  return errors
}

export function validatePostForm(form) {
  const errors = {}

  if (isBlank(form.title)) {
    errors.title = '请输入文章标题'
  } else if (String(form.title).trim().length < 2) {
    errors.title = '标题至少 2 个字符'
  }

  if (isBlank(form.content)) {
    errors.content = '请输入文章内容'
  } else if (String(form.content).trim().length < 10) {
    errors.content = '内容至少 10 个字符'
  }

  return errors
}

export function validateCreateUserForm(form) {
  const errors = {}

  if (isBlank(form.username)) {
    errors.username = '请输入用户名'
  } else if (!USERNAME_REGEX.test(String(form.username).trim())) {
    errors.username = '用户名需为 3-32 位字母、数字、下划线或中划线'
  }

  if (isBlank(form.email)) {
    errors.email = '请输入邮箱'
  } else if (!isValidEmail(String(form.email).trim())) {
    errors.email = '请输入有效的邮箱地址'
  }

  if (isBlank(form.password)) {
    errors.password = '请输入初始密码'
  } else if (String(form.password).length < 8) {
    errors.password = '初始密码长度不能少于 8 位'
  }

  return errors
}

export function validatePublishForm(form) {
  const errors = {}

  if (!form.postId) {
    errors.postId = '请选择要发布的文章'
  }

  if (!Array.isArray(form.siteIds) || form.siteIds.length === 0) {
    errors.siteIds = '请至少选择一个发布站点'
  }

  return errors
}

export function validateChangePasswordForm(form) {
  const errors = {}

  if (isBlank(form.currentPassword)) {
    errors.currentPassword = '请输入当前密码'
  }

  if (isBlank(form.newPassword)) {
    errors.newPassword = '请输入新密码'
  } else if (String(form.newPassword).length < 8) {
    errors.newPassword = '新密码长度不能少于 8 位'
  } else if (form.newPassword === form.currentPassword) {
    errors.newPassword = '新密码不能与当前密码相同'
  }

  return errors
}

export function validateUpdateUserForm(form) {
  const errors = {}

  if (isBlank(form.email)) {
    errors.email = '请输入邮箱'
  } else if (!isValidEmail(String(form.email).trim())) {
    errors.email = '请输入有效的邮箱地址'
  }

  if (isBlank(form.nickname)) {
    errors.nickname = '请输入昵称'
  } else if (String(form.nickname).trim().length > 50) {
    errors.nickname = '昵称长度不能超过 50 个字符'
  }

  return errors
}
