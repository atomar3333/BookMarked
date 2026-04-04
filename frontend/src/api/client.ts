import axios, { AxiosError } from 'axios'
import { getToken } from '../utils/tokenStorage'

const baseURL = import.meta.env.VITE_API_BASE_URL ?? ''

export const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const data = error.response?.data
    if (typeof data === 'string') {
      return Promise.reject(new Error(data))
    }

    return Promise.reject(new Error('Something went wrong. Please try again.'))
  },
)
