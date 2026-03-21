import { apiClient } from './client'
import type { BooksPageResponse } from '../types/books'

export async function getBooksPage(page: number, size: number): Promise<BooksPageResponse> {
  const response = await apiClient.get<BooksPageResponse>('/api/books/all', {
    params: { page, size },
  })
  return response.data
}
