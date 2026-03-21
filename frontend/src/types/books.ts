export interface BookTileItem {
  id: number
  title: string
  author: string
  coverImageUrl?: string
  description?: string
}

export interface BooksPageResponse {
  content: BookTileItem[]
  totalPages: number
  totalElements: number
  size: number
  number: number
}
