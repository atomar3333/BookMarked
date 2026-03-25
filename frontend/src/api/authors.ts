import { apiClient } from './client'

export interface AuthorItem {
  id: number
  authorName: string
  bio?: string
  profilePictureUrl?: string
}

export interface AuthorBookItem {
  id: number
  title: string
  author?: string
  coverImageUrl?: string
  description?: string
  publishDate?: string
}

interface PageResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  size: number
  number: number
}

export async function getAllAuthors(page = 0, size = 10): Promise<PageResponse<AuthorItem>> {
  const response = await apiClient.get<PageResponse<AuthorItem>>('/api/authors/all', {
    params: { page, size },
  })
  return response.data
}

export async function searchAuthors(name: string): Promise<AuthorItem[]> {
  const response = await apiClient.get<AuthorItem[]>('/api/authors/search', {
    params: { name },
  })
  return response.data
}

export async function getAuthorById(authorId: number): Promise<AuthorItem> {
  const response = await apiClient.get<AuthorItem>(`/api/authors/${authorId}`)
  return response.data
}

export async function getBooksByAuthor(authorId: number): Promise<AuthorBookItem[]> {
  const response = await apiClient.get<AuthorBookItem[]>(`/api/authors/${authorId}/books`)
  return response.data
}