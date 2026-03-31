import { apiClient } from './client'

export interface ReadProgressResponse {
  id: number
  userId: number
  bookId: number
  currentPosition: number
  totalPages: number
  status: string
  startedAt?: string
  finishedAt?: string
  readNumber: number
  createdAt: string
  updatedAt: string
}

export interface CreateReadProgressPayload {
  userId: number
  bookId: number
  currentPosition?: number
  totalPages?: number
  status?: string
  startedAt?: string
  readNumber?: number
}

export interface UpdateReadProgressPayload {
  currentPosition?: number
  totalPages?: number
  status?: string
  startedAt?: string
  finishedAt?: string
  readNumber?: number
}

export async function getReadProgressForUserBook(
  userId: number,
  bookId: number,
): Promise<ReadProgressResponse[]> {
  const response = await apiClient.get<ReadProgressResponse[]>(
    `/api/read-progress/users/${userId}/books/${bookId}`,
  )
  return response.data
}

export async function createReadProgress(
  payload: CreateReadProgressPayload,
): Promise<ReadProgressResponse> {
  const response = await apiClient.post<ReadProgressResponse>('/api/read-progress', payload)
  return response.data
}

export async function updateReadProgress(
  id: number,
  payload: UpdateReadProgressPayload,
): Promise<ReadProgressResponse> {
  const response = await apiClient.put<ReadProgressResponse>(`/api/read-progress/${id}`, payload)
  return response.data
}
