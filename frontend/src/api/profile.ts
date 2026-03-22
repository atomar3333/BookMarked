import axios from 'axios'
import { apiClient } from './client'
import type { UserProfileItem } from '../types/search'

interface UpdateProfileRequest {
  userName?: string
  emailId?: string
  bio?: string
}

function normalizeProfileError(error: unknown): Error {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status
    const data = error.response?.data as { message?: string }
    
    if (status === 400) {
      return new Error(data?.message || 'Invalid profile data. Please check your inputs.')
    }
    if (status === 403) {
      return new Error('You do not have permission to update this profile.')
    }
    if (status === 404) {
      return new Error('User profile not found.')
    }
    if (status === 409) {
      return new Error('Username or email is already in use. Please try another.')
    }
  }

  if (error instanceof Error) {
    return error
  }

  return new Error('Profile update failed. Please try again.')
}

export async function updateCurrentUserProfile(userId: number, payload: UpdateProfileRequest): Promise<UserProfileItem> {
  try {
    const response = await apiClient.put<UserProfileItem>(`/api/users/${userId}`, payload)
    return response.data
  } catch (error) {
    throw normalizeProfileError(error)
  }
}
