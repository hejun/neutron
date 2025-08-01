import type Page from '@/api/Page.ts'
import HttpClient from '@/api/HttpClient.ts'

const http = new HttpClient(import.meta.env.VITE_BASE_URL)

export interface Tenant {
  id?: string

  name: string

  issuer: string

  publicKey?: string

  privateKey?: string

  enabled?: string

  createDate?: string
}

export async function findTenantPage(page: number = 1, size: number = 15): Promise<Page<Tenant>> {
  return http.get('/tenant', { page, size })
}

export async function saveTenant(tenant: Tenant): Promise<{ id: number }> {
  return http.post('/tenant', tenant)
}
