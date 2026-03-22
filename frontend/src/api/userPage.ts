import { apiClient } from './client'
import type { ReadingStatusItem, UserReviewItem } from '../types/userPage'

export async function getReadingStatusesByUser(userId: number): Promise<ReadingStatusItem[]> {
  const response = await apiClient.get<ReadingStatusItem[]>(`/api/reading-status/users/${userId}`)
  return response.data
}

export async function getReviewsByUser(userId: number): Promise<UserReviewItem[]> {
  const response = await apiClient.get<UserReviewItem[]>(`/api/reviews/users/${userId}`)
  return response.data
}
