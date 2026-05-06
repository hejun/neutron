declare global {
  interface SuccessResult<T> {
    code: 200
    msg: string
    data: T | null
  }

  interface AccessDeniedResult {
    code: 401
    msg: string
    data: {
      providers: {
        clientId: string
        clientName: string
        location: string
      }[]
    }
  }

  type Result<T = void> = SuccessResult<T> | AccessDeniedResult

  interface Page<T> {
    page: {
      number: number
      size: number
      totalElements: number
      totalPages: number
    }
    content: T[]
  }
}

export {}
