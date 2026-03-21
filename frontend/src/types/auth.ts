export interface LoginRequest {
  emailId: string
  password: string
}

export interface RegisterRequest {
  userName: string
  emailId: string
  password: string
  bio: string
}

export interface AuthResponse {
  token: string
  tokenType?: string
}
