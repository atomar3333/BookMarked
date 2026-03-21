export interface BookSearchItem {
  id: number
  title: string
  author: string
  coverImageUrl?: string
  description?: string
}

export interface UserSearchItem {
  id: number
  userName: string
  emailId?: string
  bio?: string
}

export interface UnifiedSearchResult {
  books: BookSearchItem[]
  users: UserSearchItem[]
  warnings: string[]
}
