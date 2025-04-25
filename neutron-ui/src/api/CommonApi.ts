export interface Pageable {
  number: number
  size: number
  totalElements: number
  totalPages: number
}

export interface Page<T> {
  content: T[]
  page: Pageable
}
