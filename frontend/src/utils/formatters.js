export function siteStatusLabel(site) {
  if (site.status === 0) return '停用'
  if (site.provisionStatus === 'PROVISIONING') return '建站中'
  return '可用'
}

export function siteStatusClass(site) {
  if (site.status === 0) return 'status-danger'
  if (site.provisionStatus === 'PROVISIONING') return 'status-warn'
  return 'status-success'
}

export function excerpt(content) {
  if (!content) return ''
  return content.length > 140 ? `${content.slice(0, 140)}...` : content
}
