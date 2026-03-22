import { apiClient } from './client'
import type {
  ListBookEntriesPageResponse,
  ListTileItem,
  ListsPageResponse,
} from '../types/lists'

export async function getListsPage(page: number, size: number): Promise<ListsPageResponse> {
  const response = await apiClient.get<ListsPageResponse>('/api/lists/all', {
    params: { page, size },
  })
  return response.data
}

export async function getListById(listId: number): Promise<ListTileItem> {
  const response = await apiClient.get<ListTileItem>(`/api/lists/${listId}`)
  return response.data
}

export async function getBooksInList(
  listId: number,
  page: number,
  size: number,
): Promise<ListBookEntriesPageResponse> {
  const response = await apiClient.get<ListBookEntriesPageResponse>(`/api/lists/${listId}/books`, {
    params: { page, size },
  })
  return response.data
}
