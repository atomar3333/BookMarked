export interface BookSearchItem {
  id: number
  title: string
  author: string
  coverImageUrl?: string
  description?: string
}

export interface BookDetail {
  id: number
  googleBooksId?: string
  title: string
  author: string
  isbn?: string
  coverImageUrl?: string
  description?: string
  publishDate?: string
}

export interface UserSearchItem {
  id: number
  userName: string
  emailId?: string
  bio?: string
}

export interface UserProfileItem {
  id: number
  userName: string
  emailId?: string
  bio?: string
}

export interface ReviewItem {
  id: number
  userId: number
  bookId: number
  reviewText?: string
  rating: number
  createdAt?: string
}

export interface CreateReviewRequest {
  userId: number
  bookId: number
  reviewText: string
  rating: number
}

export interface UnifiedSearchResult {
  books: BookSearchItem[]
  users: UserSearchItem[]
  warnings: string[]
}
