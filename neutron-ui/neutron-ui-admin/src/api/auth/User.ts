import * as http from '@/api/HttpClient'
import type { TenantList } from '@/api/auth/Tenant.ts'

export type UserList = {
  id: string
  username: string
  email: string
  emailVerified: boolean
  phoneNumber: string
  phoneNumberVerified: boolean
  nickname: string
  gender: string
  enabled: boolean
  tenant: TenantList
  createdDate: string
  lastModifiedDate: string
}

export type UserDetail = {
  id: string
  username: string
  password?: string
  email?: string
  emailVerified?: boolean
  phoneNumber?: string
  phoneNumberVerified?: boolean
  nickname?: string
  gender?: number
  avatar?: string
  birthdate?: string
  enabled: boolean
  tenant: TenantList
  createdDate: string
  lastModifiedDate?: string
}

export async function findPage(
  username?: string,
  enabled?: string,
  tenantId?: string,
  page = 0,
  size = 10,
  sort?: string
): Promise<Page<UserList> | undefined> {
  return http.get('/auth/user', { username, enabled, tenantId, page, size, sort })
}

export async function findById(id: string): Promise<UserDetail | undefined> {
  return http.get('/auth/user/' + id)
}

export async function save(user: UserDetail): Promise<void> {
  return http.post('/auth/user', user)
}

export async function update(user: UserDetail): Promise<void> {
  return http.put('/auth/user', user)
}

export async function del(id: string): Promise<void> {
  return http.del('/auth/user/' + id)
}
