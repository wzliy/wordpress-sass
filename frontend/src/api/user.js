import { authOnlyOptions, jsonOptions, request } from './http'

export function listUsers() {
  return request('/api/users/list', authOnlyOptions())
}

export function getUserDetail(id) {
  return request(`/api/users/detail?id=${id}`, authOnlyOptions())
}

export function createUser(body) {
  return request('/api/users/create', jsonOptions('POST', body))
}

export function changePassword(body) {
  return request('/api/users/change-password', jsonOptions('POST', body))
}

export function updateUserProfile(body) {
  return request('/api/users/update', jsonOptions('POST', body))
}

export function disableUser(userId) {
  return request('/api/users/disable', jsonOptions('POST', { userId }))
}

export function enableUser(userId) {
  return request('/api/users/enable', jsonOptions('POST', { userId }))
}
