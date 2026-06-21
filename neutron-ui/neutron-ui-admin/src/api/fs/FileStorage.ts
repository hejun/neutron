import * as http from '@/api/HttpClient.ts'

export async function upload(
  file: File,
  isPublic = false,
  expireDays?: 1 | 3 | 7 | 15 | 30 | 90 | 180 | 365
): Promise<string | undefined> {
  const body = new FormData()
  body.append('file', file)
  body.set('isPublic', isPublic.toString())
  if (expireDays) {
    body.set('expireDays', expireDays.toString())
  }
  return http.post('/fs', body)
}

export async function download(file: string): Promise<string | undefined> {
  return http.get(`/fs/${file}`)
}
