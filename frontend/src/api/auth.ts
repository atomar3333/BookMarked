import { apiClient } from './client'
import type { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth'

export async function registerUser(payload: RegisterRequest): Promise<string> {
  const response = await apiClient.post<string>('/api/auth/register', payload)
  return response.data
}

export async function loginUser(payload: LoginRequest): Promise<AuthResponse> {
  const response = await apiClient.post<AuthResponse>('/api/auth/login', payload)
  return response.data
}
