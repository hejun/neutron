import * as http from '@/api/HttpClient'
import useSystemStore from '@/store/SystemStore.ts'

interface OidcUser {
  sub: string
  aud: string
  aud_name: string
  aud_logo: string
}

export async function userinfo(): Promise<OidcUser | null> {
  const systemStore = useSystemStore()
  const client = systemStore.client
  if (client) {
    return http.get(`/auth/${client.clientId}/userinfo`)
  }
  return http.get('/auth/userinfo')
}

export function logout() {
  const systemStore = useSystemStore()
  systemStore.destroyClient()
  http.redirect(`/logout?continue=${encodeURIComponent(window.location.href)}`)
}
