import { apiClient } from './client'
import type { ActivityPage, ActivityType } from '../types/activities'

export function getMyActivities(
  page = 0,
  size = 20,
  type?: ActivityType,
): Promise<ActivityPage> {
  return apiClient
    .get<ActivityPage>('/api/activities/me', { params: { page, size, type } })
    .then((r) => r.data)
}

export function getUserActivities(
  userId: number,
  page = 0,
  size = 20,
  type?: ActivityType,
): Promise<ActivityPage> {
  return apiClient
    .get<ActivityPage>(`/api/activities/users/${userId}`, { params: { page, size, type } })
    .then((r) => r.data)
}

export function getFeed(
  page = 0,
  size = 20,
  type?: ActivityType,
): Promise<ActivityPage> {
  return apiClient
    .get<ActivityPage>('/api/activities/feed', { params: { page, size, type } })
    .then((r) => r.data)
}
