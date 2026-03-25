import axios from 'axios'
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
  try {
    const response = await apiClient.get<ListTileItem>(`/api/lists/${listId}`)
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 403) {
      throw new Error('PRIVATE_LIST')
    }
    throw error
  }
}

export async function getBooksInList(
  listId: number,
  page: number,
  size: number,
): Promise<ListBookEntriesPageResponse> {
  try {
    const response = await apiClient.get<ListBookEntriesPageResponse>(`/api/lists/${listId}/books`, {
      params: { page, size },
    })
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 403) {
      throw new Error('PRIVATE_LIST')
    }
    throw error
  }
}
