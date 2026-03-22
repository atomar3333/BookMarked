import type { ReadingStatusValue } from '../types/userPage'
import { apiClient } from './client'

export interface ReadingStatusPayload {
  userId: number
  bookId: number
  currentStatus: ReadingStatusValue
}

export interface ReadingStatusResponse {
  id: number
  userId: number
  bookId: number
  currentStatus: ReadingStatusValue
  startedAt?: string
  finishedAt?: string
}

export async function getCurrentUserReadingStatuses(userId: number): Promise<ReadingStatusResponse[]> {
  const response = await apiClient.get<ReadingStatusResponse[]>(`/api/reading-status/users/${userId}`)
  return response.data
}

export async function getReadingStatusForUserBook(
  userId: number,
  bookId: number,
): Promise<ReadingStatusResponse | null> {
  try {
    const response = await apiClient.get<ReadingStatusResponse>(
      `/api/reading-status/users/${userId}/books/${bookId}`,
    )
    return response.data
  } catch {
    return null
  }
}

export async function upsertReadingStatus(payload: ReadingStatusPayload): Promise<ReadingStatusResponse> {
  // Try to update existing record first; fall back to creating a new one
  const existing = await getReadingStatusForUserBook(payload.userId, payload.bookId)
  if (existing) {
    const response = await apiClient.put<ReadingStatusResponse>(
      `/api/reading-status/${existing.id}`,
      payload,
    )
    return response.data
  }

  const response = await apiClient.post<ReadingStatusResponse>('/api/reading-status', payload)
  return response.data
}
