import { apiClient } from './client'
import type {
  BookDetail,
  BookSearchItem,
  CreateReviewRequest,
  ReviewItem,
  UnifiedSearchResult,
  UserProfileItem,
  UserSearchItem,
} from '../types/search'

async function searchBooksByTitle(query: string): Promise<BookSearchItem[]> {
  const response = await apiClient.get<BookSearchItem[]>('/api/books/search/title', {
    params: { title: query },
  })
  return dedupeById(response.data)
}

async function searchUsersByUserName(query: string): Promise<UserSearchItem[]> {
  const response = await apiClient.get<UserSearchItem[]>('/api/users/search', {
    params: { userName: query },
  })
  return dedupeById(response.data)
}

function dedupeById<T extends { id: number }>(items: T[]): T[] {
  const unique = new Map<number, T>()
  items.forEach((item) => {
    unique.set(item.id, item)
  })
  return Array.from(unique.values())
}

function asWarning(prefix: string, error: unknown): string {
  if (error instanceof Error) {
    return `${prefix}: ${error.message}`
  }
  return `${prefix}: unable to load right now.`
}

export async function getBookById(bookId: number): Promise<BookDetail> {
  const response = await apiClient.get<BookDetail>(`/api/books/${bookId}`)
  return response.data
}

export async function getBookAverageRating(bookId: number): Promise<number> {
  const response = await apiClient.get<number>(`/api/reviews/books/${bookId}/average-rating`)
  return response.data
}

export async function getBookReviews(bookId: number): Promise<ReviewItem[]> {
  const response = await apiClient.get<ReviewItem[]>(`/api/reviews/books/${bookId}`)
  return response.data
}

export async function getUserById(userId: number): Promise<UserProfileItem> {
  const response = await apiClient.get<UserProfileItem>(`/api/users/${userId}`)
  return response.data
}

export async function getCurrentUser(): Promise<UserProfileItem> {
  const response = await apiClient.get<UserProfileItem>('/api/users/me')
  return response.data
}

export async function createReview(payload: CreateReviewRequest): Promise<ReviewItem> {
  const response = await apiClient.post<ReviewItem>('/api/reviews', payload)
  return response.data
}

export async function unifiedSearch(query: string): Promise<UnifiedSearchResult> {
  const [booksResult, usersResult] = await Promise.allSettled([
    searchBooksByTitle(query),
    searchUsersByUserName(query),
  ])

  const warnings: string[] = []

  const books = booksResult.status === 'fulfilled' ? booksResult.value : []
  if (booksResult.status === 'rejected') {
    warnings.push(asWarning('Books search failed', booksResult.reason))
  }

  const users = usersResult.status === 'fulfilled' ? usersResult.value : []
  if (usersResult.status === 'rejected') {
    warnings.push(asWarning('Users search failed', usersResult.reason))
  }

  return {
    books,
    users,
    warnings,
  }
}
