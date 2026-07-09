import axios from 'axios'

const baseURL =
  import.meta.env.VITE_API_BASE_URL ||
  (import.meta.env.PROD ? 'https://farm-webapp-rezy.onrender.com' : '')

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
