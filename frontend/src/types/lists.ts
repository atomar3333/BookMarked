import type { LikeStatsDto } from './search'

export interface ListTileItem {
  id: number
  userId: number
  title: string
  description?: string
  createdDate?: string
  likeStats?: LikeStatsDto
}

export interface ListsPageResponse {
  content: ListTileItem[]
  totalPages: number
  totalElements: number
  size: number
  number: number
}

export interface ListBookEntry {
  id: number
  listId: number
  bookId: number
  addedAt?: string
}

export interface ListBookEntriesPageResponse {
  content: ListBookEntry[]
  totalPages: number
  totalElements: number
  size: number
  number: number
}
