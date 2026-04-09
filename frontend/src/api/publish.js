import { authOnlyOptions, jsonOptions, request } from './http'

export function publishPost(body) {
  return request('/api/publish', jsonOptions('POST', body))
}

export function listPublishRecords() {
  return request('/api/publish/list', authOnlyOptions())
}
