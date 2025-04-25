import type { Page } from '@/api/CommonApi.ts'
import HttpClient from '@/api/HttpClient.ts'

const http = new HttpClient(import.meta.env.VITE_BASE_URL)

export interface Tenant {
  id: string
  name: string
  enabled: boolean
  createDate: number
  lastModifiedDate: number
}

export function findTenantPage(number: number = 0, size: number = 15) {
  return http.get<Page<Tenant>>('/tenant', { number: number, size: size })
}

export function findTenantById(id: string) {
  return http.get<Tenant | null>(`/tenant/${id}`)
}

export function saveTenant(tenant: Tenant) {
  return http.post<Tenant>('/tenant', tenant)
}

export function updateTenant(tenant: Tenant) {
  return http.put<Tenant>('/tenant', tenant)
}

export function deleteTenant(id: string) {
  return http.del<void>(`/tenant/${id}`)
}
