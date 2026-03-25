import { apiClient } from './client'

export interface GenreItem {
  id: number
  genreName: string
}

export interface GenreBookItem {
  id: number
  title: string
  author?: string
  coverImageUrl?: string
  description?: string
  publishDate?: string
}

export async function getGenreById(genreId: number): Promise<GenreItem> {
  const response = await apiClient.get<GenreItem>(`/api/genres/${genreId}`)
  return response.data
}

export async function getBooksByGenre(genreId: number): Promise<GenreBookItem[]> {
  const response = await apiClient.get<GenreBookItem[]>(`/api/genres/${genreId}/books`)
  return response.data
}
