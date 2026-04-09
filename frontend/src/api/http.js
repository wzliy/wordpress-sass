export const TOKEN_KEY = 'wpss_token'
export const USER_KEY = 'wpss_user'
export const AUTH_EXPIRED_EVENT = 'wpss:auth-expired'

export class ApiError extends Error {
  constructor(message, options = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = options.status || 0
    this.code = options.code || ''
    this.details = options.details || null
    this.isAuthError = Boolean(options.isAuthError)
  }
}

function authHeaders(includeJson = false) {
  const token = localStorage.getItem(TOKEN_KEY)
  return {
    Accept: 'application/json',
    ...(includeJson ? { 'Content-Type': 'application/json' } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }
}

function clearSessionStorage() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

function emitAuthExpired(message) {
  clearSessionStorage()
  window.dispatchEvent(
    new CustomEvent(AUTH_EXPIRED_EVENT, {
      detail: {
        message: message || '登录已失效，请重新登录',
      },
    }),
  )
}

function isJsonResponse(response) {
  return response.headers.get('content-type')?.includes('application/json')
}

async function parsePayload(response) {
  if (!isJsonResponse(response)) {
    return null
  }

  try {
    return await response.json()
  } catch {
    return null
  }
}

function normalizeErrorMessage(response, payload) {
  if (payload?.message) {
    return payload.message
  }

  if (response.status >= 500) {
    return '服务暂时不可用，请稍后重试'
  }

  if (response.status === 401) {
    return '登录已失效，请重新登录'
  }

  if (response.status === 403) {
    return '当前账号没有权限执行该操作'
  }

  return '请求失败'
}

function isAuthFailure(response, payload) {
  if (response.status === 401) {
    return true
  }

  const code = String(payload?.code || '').toUpperCase()
  const message = String(payload?.message || '')
  return code === 'UNAUTHORIZED' || code === 'TOKEN_EXPIRED' || message.includes('登录已失效')
}

export async function request(url, options = {}) {
  let response

  try {
    response = await fetch(url, options)
  } catch {
    throw new ApiError('网络异常，请检查服务是否可用', {
      code: 'NETWORK_ERROR',
    })
  }

  const payload = await parsePayload(response)
  const success = payload?.success

  if (!response.ok || success === false) {
    const message = normalizeErrorMessage(response, payload)
    const authError = isAuthFailure(response, payload)

    if (authError) {
      emitAuthExpired(message)
    }

    throw new ApiError(message, {
      status: response.status,
      code: payload?.code || '',
      details: payload,
      isAuthError: authError,
    })
  }

  return payload?.data
}

export function jsonOptions(method, body, auth = true) {
  return {
    method,
    headers: auth ? authHeaders(true) : { Accept: 'application/json', 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  }
}

export function authOnlyOptions() {
  return {
    headers: authHeaders(false),
  }
}
