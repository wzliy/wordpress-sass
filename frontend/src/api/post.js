import { authOnlyOptions, jsonOptions, request } from './http'

export function listPosts() {
  return request('/api/post/list', authOnlyOptions())
}

export function createPost(body) {
  return request('/api/post/create', jsonOptions('POST', body))
}
