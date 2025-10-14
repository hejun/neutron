import type Page from '@/api/Page.ts'
import HttpClient from '@/api/HttpClient.ts'

const http = new HttpClient(import.meta.env.VITE_BASE_URL)

export interface Tenant {
  id?: string

  name: string

  issuer: string

  publicKey?: string

  privateKey?: string

  enabled?: boolean

  createDate?: string
}

export async function findTenantPage(
  current: number = 1,
  size: number = 20,
  name?: string,
  enabled?: boolean
): Promise<Page<Tenant>> {
  return http.get('/tenant', { current, size, name, enabled })
}

export async function saveTenant(tenant: Tenant): Promise<{ id: number }> {
  return http.post('/tenant', tenant)
}
