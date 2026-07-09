import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import * as apiClient from '../lib/api'
import type { User } from '../types/api'

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
    apiClient
      .fetchMe()
      .then((u) => setUser(u || null))
      .catch(() => setUser(null))
      .finally(() => setIsLoading(false))
  }, [])

  const login = async (email: string, password: string, rememberMe: boolean) => {
    const u = await apiClient.login(email, password, rememberMe)
    setUser(u || null)
  }

  const register = async (name: string, email: string, password: string, passwordConfirm: string, adminCode?: string) => {
    const u = await apiClient.register({
      name,
      email,
      password,
      password_confirm: passwordConfirm,
      admin_code: adminCode,
    })
    setUser(u || null)
  }

  const logout = async () => {
    await apiClient.logout()
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
