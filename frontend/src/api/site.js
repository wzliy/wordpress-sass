import { authOnlyOptions, jsonOptions, request } from './http'

export function listSites() {
  return request('/api/site/list', authOnlyOptions())
}

export function listSiteTemplates() {
  return request('/api/site/template/list', authOnlyOptions())
}

export function getSiteWorkspace(id) {
  return request(`/api/site/workspace?id=${id}`, authOnlyOptions())
}

export function registerSite(body) {
  return request('/api/site/register', jsonOptions('POST', body))
}

export function provisionSite(body) {
  return request('/api/site/provision', jsonOptions('POST', body))
}

export function testSiteConnection(id) {
  return request(`/api/site/test?id=${id}`, authOnlyOptions())
}
