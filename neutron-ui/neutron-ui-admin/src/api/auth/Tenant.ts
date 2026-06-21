import * as http from '@/api/HttpClient'

export type TenantList = {
  id: string
  name: string
  issuer: string
  enabled: boolean
  createdDate: string
  lastModifiedDate?: string
}

export type TenantDetail = {
  id: string
  name: string
  issuer: string
  copyright?: string
  enabled: boolean
  createdDate: string
  lastModifiedDate?: string
}

export async function findPage(
  name?: string,
  enabled?: boolean,
  page = 0,
  size = 10,
  sort?: string
): Promise<Page<TenantList> | undefined> {
  return http.get('/auth/tenant', { name, enabled, page, size, sort })
}

export async function findById(id: string): Promise<TenantDetail | undefined> {
  return http.get('/auth/tenant/' + id)
}

export async function save(tenant: TenantDetail): Promise<void> {
  return http.post('/auth/tenant', tenant)
}

export async function update(tenant: TenantDetail): Promise<void> {
  return http.put('/auth/tenant', tenant)
}

export async function del(id: string): Promise<void> {
  return http.del('/auth/tenant/' + id)
}