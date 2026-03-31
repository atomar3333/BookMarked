import { apiClient } from './client'

export interface CreateReadingSessionPayload {
  userId: number
  readProgressId: number
  durationMinutes: number
  startPosition: number
  endPosition: number
  sessionDate: string
  notes?: string
}

export interface ReadingSessionResponse {
  id: number
  userId: number
  readProgressId: number
  durationMinutes: number
  startPosition: number
  endPosition: number
  sessionDate: string
  notes?: string
  createdAt: string
}

export async function createReadingSession(
  payload: CreateReadingSessionPayload,
): Promise<ReadingSessionResponse> {
  const response = await apiClient.post<ReadingSessionResponse>('/api/reading-sessions', payload)
  return response.data
}
