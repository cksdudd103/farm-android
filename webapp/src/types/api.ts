export interface User {
  id: number
  name: string
  email: string
  role: 'admin' | 'farmer'
  phone?: string
  farm_name?: string
  region?: string
  is_active_user?: boolean
  created_at?: string
  plan_code?: string
  plan_name?: string
  subscription_status?: string
  subscription_expiry?: string
  billing_cycle?: string
  is_waived?: boolean
  grade_id?: number | null
  grade_code?: string
  grade_name?: string
  grade_discount?: number
}

export interface ApiResponse<T> {
  ok: boolean
  data?: T
  msg?: string
}

export interface AuthResponse {
  ok: boolean
  user?: User
  msg?: string
}

export interface Crop {
  id: number
  user_id: number
  name: string
  variety?: string
  field_location?: string
  area?: number
  planting_date?: string
  expected_harvest_date?: string
  status: string
  memo?: string
  image?: string
  created_at?: string
}

export interface Journal {
  id: number
  user_id: number
  crop_id?: number | null
  crop_name?: string
  date: string
  work_type?: string
  weather?: string
  content?: string
  image?: string
  created_at?: string
}

export interface Task {
  id: number
  user_id: number
  crop_id?: number | null
  crop_name?: string
  title: string
  memo?: string
  due_date?: string
  priority: string
  status: string
  created_at?: string
}

export interface InventoryItem {
  id: number
  user_id: number
  name: string
  category?: string
  quantity: number
  unit?: string
  location?: string
  expiry_date?: string
  memo?: string
  created_at?: string
}

export interface Diagnosis {
  id: number
  user_id: number
  crop_name?: string
  image?: string
  disease_name?: string
  confidence?: number
  severity?: string
  advice?: string
  created_at?: string
}

export interface MarketItem {
  name: string
  unit: string
  price: number
  change_pct: number
  trend: 'up' | 'down' | 'flat'
}

export interface MarketResponse {
  ok: boolean
  data: MarketItem[]
  date?: string
}

export interface WeatherDay {
  date: string
  day: string
  condition: string
  temp_max: number
  temp_min: number
  humidity: number
  rain_prob: number
}

export interface CurrentWeather {
  temp: number
  condition: string
  wind_speed: number
  humidity: number
}

export interface WeatherResponse {
  ok: boolean
  region?: string
  current?: CurrentWeather
  data: WeatherDay[]
}

export interface PesticideInfo {
  name: string
  type?: string
  category?: string
  ingredient?: string
  target: string
  crops?: string
  safety_period?: string
  dilution?: string
}

export interface SupportProgram {
  title: string
  agency: string
  period: string
  target: string
  content: string
  status: string
}

export interface DashboardSummary {
  total_crops: number
  growing_crops: number
  pending_tasks: number
  today_tasks: number
  low_stock: number
  total_shipment_amount: number
  recent_journals: Journal[]
  upcoming_tasks: Task[]
  chart_labels: string[]
  chart_counts: number[]
  crop_status_counts: Record<string, number>
}

export interface DashboardResponse {
  ok: boolean
  data?: DashboardSummary
}
