import * as http from '@/api/HttpClient'

export async function connect(): Promise<EventSource> {
  return http.sse('/notify/msg/connect')
}

export async function sendMsg(receiver: string, content: string): Promise<void> {
  return http.post('/notify/msg/send', { receiver, content })
}
