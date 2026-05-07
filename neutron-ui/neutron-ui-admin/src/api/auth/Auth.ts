import * as http from '@/api/HttpClient'

interface OidcUser {
  sub: string
  name: string
  nickname: string
  picture: string
  gender: string
  birthdate: string
  address: string
  email: string
  email_verified: boolean
  phone_number_verified: boolean
  phone_number: string
  updated_at: string
  aud: string
  aud_name: string
}

export async function userinfo(): Promise<OidcUser | undefined> {
  return http.get('/userinfo')
}

export function logout() {
  http.redirect(`/logout?continue=${encodeURIComponent(window.location.href)}`)
}
