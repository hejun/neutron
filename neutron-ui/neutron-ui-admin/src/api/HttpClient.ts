import 'element-plus/theme-chalk/el-message-box.css'
import { ElMessageBox } from 'element-plus'

const BASE_URL = import.meta.env.VITE_BASE_URL || ''

async function request<T>(url: string, init?: RequestInit, timeout: number = 5000): Promise<T | undefined> {
  const controller = new AbortController()
  init = init ? { ...init } : {}
  init.signal = controller.signal

  // 请求超时后中止请求
  const timer = setTimeout(() => {
    controller.abort()
  }, timeout)
  const resp = await fetch(url, init)
  clearTimeout(timer)

  const contentType = resp.headers.get('Content-Type') || ''

  if (resp.status !== 200) {
    if (contentType.includes('application/json')) {
      return Promise.reject(await resp.json())
    }
    return Promise.reject((await resp.text()) || `HTTP Error ${resp.status}`)
  }

  if (contentType.includes('application/json')) {
    const parsedResp = (await resp.json()) as Result<T>

    if (parsedResp.code === 200) {
      return parsedResp.data
    }

    if (parsedResp.code === 401) {
      const location = parsedResp.data.location
      await ElMessageBox.alert('您还未登录, 点击按钮开始登录', '需要登录', {
        confirmButtonText: '去登录',
        callback: () => {
          redirect(`${location}?continue=${encodeURIComponent(window.location.href)}`)
        }
      })
    }

    return Promise.reject(parsedResp)
  }

  return (await resp.text()) as T
}

function combinationRequestUrl(url: string, query?: Record<string, unknown>): string {
  const targetUrl = url.startsWith('http') ? url : `${BASE_URL}${url}`
  if (!query) return targetUrl

  const searchParams = new URLSearchParams()

  for (const [key, value] of Object.entries(query)) {
    if (value === undefined || value === null) continue
    if (typeof value === 'string' && value.trim().length === 0) continue
    if (Array.isArray(value) && value.length === 0) continue

    if (Array.isArray(value)) {
      value.forEach(item => {
        if (item !== undefined && item !== null) {
          searchParams.append(key, item.toString())
        }
      })
    } else {
      searchParams.append(key, value.toString())
    }
  }

  const queryString = searchParams.toString()
  if (!queryString) return targetUrl

  return targetUrl.includes('?') ? `${targetUrl}&${queryString}` : `${targetUrl}?${queryString}`
}

function decisionBody(body?: object | BodyInit): { body: BodyInit | null; headers: Record<string, string> } {
  const headers: Record<string, string> = { Accept: 'application/json' }

  if (body === undefined || body === null) {
    return { body: null, headers }
  }

  if (body instanceof FormData) {
    return { body, headers }
  }

  if (body instanceof URLSearchParams || body instanceof Blob || body instanceof ArrayBuffer) {
    return { body, headers }
  }

  if (Object.prototype.toString.call(body) === '[object Object]') {
    headers['Content-Type'] = 'application/json;charset=UTF-8'
    return { body: JSON.stringify(body), headers }
  }

  headers['Content-Type'] = 'application/x-www-form-urlencoded'
  return { body: body as BodyInit, headers }
}

export async function get<T>(url: string, query?: Record<string, unknown>): Promise<T | undefined> {
  return request<T>(combinationRequestUrl(url, query), {
    method: 'GET',
    headers: { Accept: 'application/json' }
  })
}

export async function post<T>(
  url: string,
  body?: object | BodyInit,
  query?: Record<string, unknown>
): Promise<T | undefined> {
  return request<T>(combinationRequestUrl(url, query), {
    method: 'POST',
    ...decisionBody(body)
  })
}

export async function put<T>(
  url: string,
  body?: object | BodyInit,
  query?: Record<string, unknown>
): Promise<T | undefined> {
  return request<T>(combinationRequestUrl(url, query), {
    method: 'PUT',
    ...decisionBody(body)
  })
}

export async function del<T>(
  url: string,
  body?: object | BodyInit,
  query?: Record<string, unknown>
): Promise<T | undefined> {
  return request<T>(combinationRequestUrl(url, query), {
    method: 'DELETE',
    ...decisionBody(body)
  })
}

export function redirect(url: string): void {
  window.location.href = url.startsWith('http') ? url : `${BASE_URL}${url}`
}

export function sse(url: string): EventSource {
  return new EventSource(`${BASE_URL}${url}`)
}
