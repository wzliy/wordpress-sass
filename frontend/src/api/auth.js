import { authOnlyOptions, jsonOptions, request } from './http'

export function login(body) {
  return request('/api/auth/login', jsonOptions('POST', body, false))
}

export function currentUser() {
  return request('/api/auth/me', authOnlyOptions())
}
