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
import { LinksPage } from './pages/LinksPage'
import { ShipmentPage } from './pages/ShipmentPage'
import { CommunityPage } from './pages/CommunityPage'
import { PlansPage } from './pages/PlansPage'
import { CheckoutPage } from './pages/CheckoutPage'
import { PaymentSuccessPage } from './pages/PaymentSuccessPage'
import { PaymentFailPage } from './pages/PaymentFailPage'

import { AdminUsersPage } from './pages/AdminUsersPage'

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
          <Route path="shipments" element={<ShipmentPage />} />
          <Route path="community" element={<CommunityPage />} />
          <Route path="weather" element={<WeatherPage />} />
          <Route path="pesticide" element={<PesticidePage />} />
          <Route path="support" element={<SupportPage />} />
          <Route path="links" element={<LinksPage />} />
          <Route path="settings" element={<SettingsPage />} />
          <Route path="plans" element={<PlansPage />} />
          <Route path="checkout" element={<CheckoutPage />} />
          <Route path="payments/success" element={<PaymentSuccessPage />} />
          <Route path="payments/fail" element={<PaymentFailPage />} />
          <Route path="admin/users" element={<AdminUsersPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </AuthProvider>
  )
}

export default App
