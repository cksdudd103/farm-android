import axios from 'axios'
import type {
  ApiResponse,
  AuthResponse,
  Crop,
  DashboardResponse,
  Diagnosis,
  InventoryItem,
  Journal,
  MarketResponse,
  PesticideInfo,
  SupportProgram,
  Task,
  User,
  WeatherResponse,
} from '../types/api'

const BACKEND_URL = 'https://farm-webapp-rezy.onrender.com'

const baseURL = import.meta.env.VITE_API_BASE_URL || BACKEND_URL

export const api = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

if (import.meta.env.DEV) {
  api.interceptors.request.use((config) => {
    console.log('[API request]', config.method?.toUpperCase(), config.url)
    return config
  })
  api.interceptors.response.use(
    (res) => {
      console.log('[API response]', res.config.url, res.status)
      return res
    },
    (err) => {
      console.error('[API error]', err.config?.url, err.response?.status, err.response?.data)
      return Promise.reject(err)
    }
  )
}

function unwrap<T>(res: { data: ApiResponse<T> }): T {
  const { data } = res
  if (!data.ok) throw new Error(data.msg || '요청에 실패했습니다.')
  if (data.data === undefined) throw new Error('응답 데이터가 없습니다.')
  return data.data
}

// Auth
export const login = (email: string, password: string, rememberMe = true) =>
  api.post<AuthResponse>('/api/login', { email, password, remember_me: rememberMe }).then((res) => res.data.user)

export const register = (payload: {
  name: string
  email: string
  password: string
  password_confirm: string
  admin_code?: string
}) => api.post<AuthResponse>('/api/register', payload).then((res) => res.data.user)

export const logout = () => api.post<ApiResponse<unknown>>('/api/logout')

export const fetchMe = () => api.get<AuthResponse>('/api/me').then((res) => res.data.user)

// Dashboard
export const fetchDashboardSummary = () => api.get<DashboardResponse>('/api/dashboard/summary').then((res) => res.data.data)

// Users (admin)
export const fetchUsers = () => api.get<ApiResponse<User[]>>('/api/users').then(unwrap)
export const updateUser = (id: number, payload: Partial<User>) =>
  api.put<ApiResponse<User>>(`/api/users/${id}`, payload).then(unwrap)
export const deleteUser = (id: number) => api.delete<ApiResponse<unknown>>(`/api/users/${id}`).then(unwrap)

// Crops
export const fetchCrops = () => api.get<ApiResponse<Crop[]>>('/api/crops').then(unwrap)
export const createCrop = (payload: Partial<Crop>) =>
  api.post<ApiResponse<Crop>>('/api/crops', payload).then(unwrap)
export const updateCrop = (id: number, payload: Partial<Crop>) =>
  api.put<ApiResponse<Crop>>(`/api/crops/${id}`, payload).then(unwrap)
export const deleteCrop = (id: number) => api.delete<ApiResponse<unknown>>(`/api/crops/${id}`).then(unwrap)

// Journals
export const fetchJournals = () => api.get<ApiResponse<Journal[]>>('/api/journals').then(unwrap)
export const createJournal = (payload: Partial<Journal>) =>
  api.post<ApiResponse<Journal>>('/api/journals', payload).then(unwrap)
export const updateJournal = (id: number, payload: Partial<Journal>) =>
  api.put<ApiResponse<Journal>>(`/api/journals/${id}`, payload).then(unwrap)
export const deleteJournal = (id: number) => api.delete<ApiResponse<unknown>>(`/api/journals/${id}`).then(unwrap)

// Tasks
export const fetchTasks = () => api.get<ApiResponse<Task[]>>('/api/tasks').then(unwrap)
export const createTask = (payload: Omit<Task, 'id' | 'user_id' | 'created_at'>) =>
  api.post<ApiResponse<Task>>('/api/tasks', payload).then(unwrap)
export const updateTask = (id: number, payload: Partial<Task>) =>
  api.put<ApiResponse<Task>>(`/api/tasks/${id}`, payload).then(unwrap)
export const deleteTask = (id: number) => api.delete<ApiResponse<unknown>>(`/api/tasks/${id}`).then(unwrap)

// Inventory
export const fetchInventory = () => api.get<ApiResponse<InventoryItem[]>>('/api/inventory').then(unwrap)
export const createInventory = (payload: Omit<InventoryItem, 'id' | 'user_id' | 'created_at'>) =>
  api.post<ApiResponse<InventoryItem>>('/api/inventory', payload).then(unwrap)
export const updateInventory = (id: number, payload: Partial<InventoryItem>) =>
  api.put<ApiResponse<InventoryItem>>(`/api/inventory/${id}`, payload).then(unwrap)
export const deleteInventory = (id: number) => api.delete<ApiResponse<unknown>>(`/api/inventory/${id}`).then(unwrap)

// Diagnoses
export const fetchDiagnoses = () => api.get<ApiResponse<Diagnosis[]>>('/api/diagnoses').then(unwrap)
export const createDiagnosis = (payload: FormData) =>
  api
    .post<ApiResponse<Diagnosis>>('/api/diagnoses', payload, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then(unwrap)
export const deleteDiagnosis = (id: number) => api.delete<ApiResponse<unknown>>(`/api/diagnoses/${id}`).then(unwrap)

// Market / Weather / Pesticide / Support
export const fetchMarketPrices = () => api.get<MarketResponse>('/api/market').then((res) => res.data.data)
export const fetchWeather = (region = '전국') => api.get<WeatherResponse>('/api/weather', { params: { region } }).then((res) => res.data)
export const fetchPesticides = (q = '') => api.get<ApiResponse<PesticideInfo[]>>('/api/pesticides', { params: { q } }).then(unwrap)
export const fetchSupportPrograms = () => api.get<ApiResponse<SupportProgram[]>>('/api/support-programs').then(unwrap)

// External agriculture sites RSS/announcements proxy
export const fetchExternalLinks = () =>
  api.get<ApiResponse<ExternalLink[]>>('/api/external-links').then((res) => res.data.data ?? [])
export const fetchAnnouncements = (source?: string) =>
  api.get<ApiResponse<Announcement[]>>('/api/announcements', { params: source ? { source } : {} }).then((res) => res.data.data ?? [])

export interface ExternalLink {
  name: string
  url: string
  category: string
  description: string
}

export interface Announcement {
  title: string
  url: string
  source: string
  date?: string
  summary?: string
}
