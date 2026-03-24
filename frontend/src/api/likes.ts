import axios from 'axios'
import { apiClient } from './client'
import type { LikeStatsDto } from '../types/search'

export interface LikeDto {
  id: number
  userId: number
  userName: string
  targetId: number
  createdAt?: string
}

export interface PageResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  number: number
  size: number
}

// Book Likes
export async function likeBook(bookId: number): Promise<void> {
  try {
    await apiClient.post(`/api/books/${bookId}/likes`)
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 409) {
      throw new Error('You have already liked this book.')
    }
    throw error
  }
}

export async function unlikeBook(bookId: number): Promise<void> {
  await apiClient.delete(`/api/books/${bookId}/likes/me`)
}

export async function getBookLikeStats(bookId: number): Promise<LikeStatsDto> {
  const response = await apiClient.get<LikeStatsDto>(`/api/books/${bookId}/likes/stats`)
  return response.data
}

export async function getBookLikes(bookId: number, page = 0, size = 10): Promise<PageResponse<LikeDto>> {
  const response = await apiClient.get<PageResponse<LikeDto>>(`/api/books/${bookId}/likes`, {
    params: { page, size },
  })
  return response.data
}

// Review Likes
export async function likeReview(reviewId: number): Promise<void> {
  try {
    await apiClient.post(`/api/reviews/${reviewId}/likes`)
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 409) {
      throw new Error('You have already liked this review.')
    }
    throw error
  }
}

export async function unlikeReview(reviewId: number): Promise<void> {
  await apiClient.delete(`/api/reviews/${reviewId}/likes/me`)
}

export async function getReviewLikeStats(reviewId: number): Promise<LikeStatsDto> {
  const response = await apiClient.get<LikeStatsDto>(`/api/reviews/${reviewId}/likes/stats`)
  return response.data
}

export async function getReviewLikes(reviewId: number, page = 0, size = 10): Promise<PageResponse<LikeDto>> {
  const response = await apiClient.get<PageResponse<LikeDto>>(`/api/reviews/${reviewId}/likes`, {
    params: { page, size },
  })
  return response.data
}

// List Likes
export async function likeList(listId: number): Promise<void> {
  try {
    await apiClient.post(`/api/lists/${listId}/likes`)
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 409) {
      throw new Error('You have already liked this list.')
    }
    throw error
  }
}

export async function unlikeList(listId: number): Promise<void> {
  await apiClient.delete(`/api/lists/${listId}/likes/me`)
}

export async function getListLikeStats(listId: number): Promise<LikeStatsDto> {
  const response = await apiClient.get<LikeStatsDto>(`/api/lists/${listId}/likes/stats`)
  return response.data
}

export async function getListLikes(listId: number, page = 0, size = 10): Promise<PageResponse<LikeDto>> {
  const response = await apiClient.get<PageResponse<LikeDto>>(`/api/lists/${listId}/likes`, {
    params: { page, size },
  })
  return response.data
}
