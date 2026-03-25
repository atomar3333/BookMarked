export interface AuthorLinkItem {
  id: number
  authorName: string
}

export interface BookTileItem {
  id: number
  title: string
  author: string
  authors?: AuthorLinkItem[]
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
