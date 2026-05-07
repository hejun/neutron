declare global {
  interface SuccessResult<T> {
    code: 200
    msg: string
    data?: T
  }

  interface AccessDeniedResult {
    code: 401
    msg: string
    data: {
      location: string
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
