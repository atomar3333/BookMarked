import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { clearToken, getToken, setToken } from '../utils/tokenStorage'

interface AuthContextType {
  token: string | null
  isAuthenticated: boolean
  login: (newToken: string) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setCurrentToken] = useState<string | null>(null)

  useEffect(() => {
    setCurrentToken(getToken())
  }, [])

  const value = useMemo<AuthContextType>(
    () => ({
      token,
      isAuthenticated: Boolean(token),
      login: (newToken: string) => {
        setToken(newToken)
        setCurrentToken(newToken)
      },
      logout: () => {
        clearToken()
        setCurrentToken(null)
      },
    }),
    [token],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
