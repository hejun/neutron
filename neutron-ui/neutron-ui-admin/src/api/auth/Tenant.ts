import * as http from '@/api/HttpClient'

export interface TenantList {
  id: string
  name: string
  issuer: string
  enabled: boolean
  createdDate: string
}

export async function findPage(
  name: string | null,
  enabled: boolean | null,
  page = 0,
  size = 10,
  sort?: string
): Promise<Page<TenantList> | null> {
  return http.get('/auth/tenant', { name, enabled, page, size, sort })
}

export async function save(name: string, issuer: string, enabled: boolean = true): Promise<{ id: string } | null> {
  return http.post('/auth/tenant', { name, issuer, enabled })
}
