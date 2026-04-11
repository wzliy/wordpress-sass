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

export function listSitePages(siteId) {
  return request(`/api/admin/sites/${siteId}/pages`, authOnlyOptions())
}

export function getSitePageEditor(siteId, pageKey) {
  return request(`/api/admin/sites/${siteId}/pages/${pageKey}/editor`, authOnlyOptions())
}

export function listSitePageVersions(siteId, pageKey) {
  return request(`/api/admin/sites/${siteId}/pages/${pageKey}/versions`, authOnlyOptions())
}

export function saveSitePageDraft(siteId, pageKey, body) {
  return request(`/api/admin/sites/${siteId}/pages/${pageKey}/draft`, jsonOptions('PUT', body))
}

export function previewSitePage(siteId, pageKey) {
  return request(`/api/admin/sites/${siteId}/pages/${pageKey}/preview`, jsonOptions('POST', {}))
}

export function publishSitePage(siteId, pageKey) {
  return request(`/api/admin/sites/${siteId}/pages/${pageKey}/publish`, jsonOptions('POST', {}))
}

export function rollbackSitePageVersion(siteId, pageKey, versionId, body = {}) {
  return request(`/api/admin/sites/${siteId}/pages/${pageKey}/versions/${versionId}/rollback`, jsonOptions('POST', body))
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
