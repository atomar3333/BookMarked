import { apiClient } from './client'
import type { PageResponse } from '../types/search'

export interface FollowerItem {
  id: number
  followerId: number
  followingId: number
}

interface FollowerCountResponse {
  userId: number
  followerCount: number
}

interface FollowingCountResponse {
  userId: number
  followingCount: number
}

export async function followUser(followerId: number, followingId: number): Promise<FollowerItem> {
  const response = await apiClient.post<FollowerItem>(
    `/api/followers/${followerId}/follow/${followingId}`,
  )
  return response.data
}

export async function unfollowUser(followerId: number, followingId: number): Promise<void> {
  await apiClient.delete(`/api/followers/${followerId}/unfollow/${followingId}`)
}

export async function getFollowers(
  userId: number,
  page = 0,
  size = 20,
): Promise<PageResponse<FollowerItem>> {
  const response = await apiClient.get<PageResponse<FollowerItem>>(`/api/followers/${userId}/followers`, {
    params: { page, size },
  })
  return response.data
}

export async function getFollowing(
  userId: number,
  page = 0,
  size = 20,
): Promise<PageResponse<FollowerItem>> {
  const response = await apiClient.get<PageResponse<FollowerItem>>(`/api/followers/${userId}/following`, {
    params: { page, size },
  })
  return response.data
}

export async function getFollowerCount(userId: number): Promise<number> {
  const response = await apiClient.get<FollowerCountResponse>(`/api/followers/${userId}/followers/count`)
  return response.data.followerCount
}

export async function getFollowingCount(userId: number): Promise<number> {
  const response = await apiClient.get<FollowingCountResponse>(`/api/followers/${userId}/following/count`)
  return response.data.followingCount
}

export async function getIsFollowing(followerId: number, followingId: number): Promise<boolean> {
  const totalFollowing = await getFollowingCount(followerId)
  const response = await getFollowing(followerId, 0, Math.max(totalFollowing, 1))
  return response.content.some((item) => item.followingId === followingId)
}
