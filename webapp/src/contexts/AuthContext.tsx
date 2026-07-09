import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { api } from '../lib/api'

interface User {
  id: number
  email: string
  name: string
  role: 'admin' | 'farmer'
}

interface AuthContextType {
  user: User | null
  isLoading: boolean
  login: (email: string, password: string, rememberMe: boolean) => Promise<void>
  register: (name: string, email: string, password: string, passwordConfirm: string, adminCode?: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    api.get('/api/me')
      .then((res) => setUser(res.data))
      .catch(() => setUser(null))
      .finally(() => setIsLoading(false))
  }, [])

  const login = async (email: string, password: string, rememberMe: boolean) => {
    const res = await api.post('/api/login', { email, password, remember_me: rememberMe })
    setUser(res.data.user)
  }

  const register = async (name: string, email: string, password: string, passwordConfirm: string, adminCode?: string) => {
    await api.post('/api/register', { name, email, password, password_confirm: passwordConfirm, admin_code: adminCode })
    const me = await api.get('/api/me')
    setUser(me.data)
  }

  const logout = async () => {
    await api.post('/api/logout')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
