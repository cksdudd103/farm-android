import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import { Layout } from './components/Layout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { CropsPage } from './pages/CropsPage'
import { TasksPage } from './pages/TasksPage'
import { JournalPage } from './pages/JournalPage'
import { InventoryPage } from './pages/InventoryPage'
import { DiagnosePage } from './pages/DiagnosePage'
import { MarketPage } from './pages/MarketPage'
import { WeatherPage } from './pages/WeatherPage'
import { PesticidePage } from './pages/PesticidePage'
import { SupportPage } from './pages/SupportPage'
import { SettingsPage } from './pages/SettingsPage'

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
          <Route index element={<DashboardPage />} />
          <Route path="crops" element={<CropsPage />} />
          <Route path="tasks" element={<TasksPage />} />
          <Route path="journal" element={<JournalPage />} />
          <Route path="inventory" element={<InventoryPage />} />
          <Route path="diagnose" element={<DiagnosePage />} />
          <Route path="market" element={<MarketPage />} />
          <Route path="weather" element={<WeatherPage />} />
          <Route path="pesticide" element={<PesticidePage />} />
          <Route path="support" element={<SupportPage />} />
          <Route path="settings" element={<SettingsPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </AuthProvider>
  )
}

export default App
