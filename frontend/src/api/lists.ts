import axios from 'axios'
import { apiClient } from './client'
import type { CreateListRequest, ListItem, PageResponse } from '../types/search'

interface UpdateListRequest {
  title: string
  description: string
  isPublic?: boolean
}

function normalizeListError(error: unknown): Error {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status
    if (status === 400) {
      return new Error('This book is already in that list, or the request is invalid.')
    }
    if (status === 403) {
      return new Error('You do not have permission to modify this list.')
    }
    if (status === 404) {
      return new Error('The selected list could not be found.')
    }
  }

  if (error instanceof Error) {
    return error
  }

  return new Error('List action failed. Please try again.')
}

export async function getListsByUser(
  userId: number,
  page = 0,
  size = 50,
): Promise<PageResponse<ListItem>> {
  const response = await apiClient.get<PageResponse<ListItem>>(`/api/lists/users/${userId}`, {
    params: { page, size },
  })
  return response.data
}

export async function createList(payload: CreateListRequest): Promise<ListItem> {
  try {
    const response = await apiClient.post<ListItem>('/api/lists', payload)
    return response.data
  } catch (error) {
    throw normalizeListError(error)
  }
}

export async function addBookToList(listId: number, bookId: number): Promise<void> {
  try {
    await apiClient.post(`/api/lists/${listId}/books/${bookId}`)
  } catch (error) {
    throw normalizeListError(error)
  }
}

export async function removeBookFromList(listId: number, bookId: number): Promise<void> {
  try {
    await apiClient.delete(`/api/lists/${listId}/books/${bookId}`)
  } catch (error) {
    throw normalizeListError(error)
  }
}

export async function updateList(listId: number, payload: UpdateListRequest): Promise<ListItem> {
  try {
    const response = await apiClient.put<ListItem>(`/api/lists/${listId}`, payload)
    return response.data
  } catch (error) {
    throw normalizeListError(error)
  }
}

export async function deleteList(listId: number): Promise<void> {
  try {
    await apiClient.delete(`/api/lists/${listId}`)
  } catch (error) {
    throw normalizeListError(error)
  }
}
