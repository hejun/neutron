import * as http from '@/api/HttpClient'
import type { TenantList } from '@/api/auth/Tenant.ts'

export type ClientList = {
  id: string
  clientId: string
  clientName: string
  authorizationGrantTypes: string[]
  enabled: boolean
  tenant: TenantList
  createdDate: string
  lastModifiedDate: string
}

export type ClientDetail = {
  id: string
  clientId: string
  clientSecret?: string
  clientName?: string
  clientAuthenticationMethods?: string[]
  authorizationGrantTypes?: string[]
  redirectUris?: string[]
  postLogoutRedirectUris?: string[]
  scopes?: string[]
  requireProofKey?: boolean
  requireAuthorizationConsent?: boolean
  accessTokenTimeToLive?: number
  refreshTokenTimeToLive?: number
  enabled: boolean
  tenant: TenantList
  createdDate: string
  lastModifiedDate?: string
}

export async function findPage(
  clientId?: string,
  clientName?: string,
  enabled?: string,
  tenantId?: string,
  page = 0,
  size = 10,
  sort?: string
): Promise<Page<ClientList> | undefined> {
  return http.get('/auth/client', { clientId, clientName, enabled, tenantId, page, size, sort })
}

export async function findById(id: string): Promise<ClientDetail | undefined> {
  return http.get('/auth/client/' + id)
}

export async function save(client: ClientDetail): Promise<void> {
  return http.post('/auth/client', client)
}

export async function update(client: ClientDetail): Promise<void> {
  return http.put('/auth/client', client)
}

export async function del(id: string): Promise<void> {
  return http.del('/auth/client/' + id)
}
