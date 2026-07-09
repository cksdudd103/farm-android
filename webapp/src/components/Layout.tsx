import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { Sprout, LayoutDashboard, Calendar, ClipboardList, Leaf, Search, CloudSun, ShoppingCart, FlaskConical, FileText, Menu, LogOut, Settings } from 'lucide-react'
import { useState } from 'react'
import type { ReactNode } from 'react'

const navItems = [
  { to: '/', icon: LayoutDashboard, label: '대시보드' },
  { to: '/crops', icon: Leaf, label: '작물 관리' },
  { to: '/journal', icon: ClipboardList, label: '영농 일지' },
  { to: '/tasks', icon: Calendar, label: '작업 일정' },
  { to: '/inventory', icon: ShoppingCart, label: '재고 관리' },
  { to: '/diagnose', icon: Search, label: '병해충 진단' },
  { to: '/market', icon: ShoppingCart, label: '농산물 시세' },
  { to: '/weather', icon: CloudSun, label: '날씨' },
  { to: '/pesticide', icon: FlaskConical, label: '농약 정보' },
  { to: '/support', icon: FileText, label: '지원사업' },
]

export function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [menuOpen, setMenuOpen] = useState(false)

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex flex-col">
      {/* Header */}
      <header className="bg-green-700 text-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 h-14 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Sprout className="w-6 h-6" />
            <span className="font-bold text-lg">스마트영농</span>
          </div>
          <div className="flex items-center gap-4">
            <span className="text-sm hidden sm:inline">{user?.name}님</span>
            <button
              onClick={() => navigate('/settings')}
              className="p-2 rounded-full hover:bg-green-600 transition"
              title="설정"
            >
              <Settings className="w-5 h-5" />
            </button>
            <button
              onClick={handleLogout}
              className="p-2 rounded-full hover:bg-green-600 transition"
              title="로그아웃"
            >
              <LogOut className="w-5 h-5" />
            </button>
            <button
              onClick={() => setMenuOpen(!menuOpen)}
              className="md:hidden p-2 rounded-full hover:bg-green-600 transition"
            >
              <Menu className="w-5 h-5" />
            </button>
          </div>
        </div>
      </header>

      <div className="flex flex-1 max-w-7xl mx-auto w-full">
        {/* Sidebar */}
        <aside
          className={`${
            menuOpen ? 'translate-x-0' : '-translate-x-full'
          } fixed inset-y-0 left-0 z-40 w-64 bg-white shadow-lg transform transition-transform duration-200 md:translate-x-0 md:static md:shadow-none md:border-r md:border-gray-200`}
        >
          <nav className="p-4 space-y-1">
            {navItems.map((item) => {
              const Icon = item.icon
              const active = location.pathname === item.to
              return (
                <Link
                  key={item.to}
                  to={item.to}
                  onClick={() => setMenuOpen(false)}
                  className={`flex items-center gap-3 px-4 py-3 rounded-lg transition ${
                    active
                      ? 'bg-green-100 text-green-800 font-semibold'
                      : 'text-gray-700 hover:bg-green-50'
                  }`}
                >
                  <Icon className="w-5 h-5" />
                  {item.label}
                </Link>
              )
            })}
          </nav>
        </aside>

        {/* Backdrop for mobile */}
        {menuOpen && (
          <div
            className="fixed inset-0 bg-black/30 z-30 md:hidden"
            onClick={() => setMenuOpen(false)}
          />
        )}

        {/* Main content */}
        <main className="flex-1 p-4 sm:p-6 overflow-auto">
          <OutletWrapper />
        </main>
      </div>
    </div>
  )
}

import { Outlet } from 'react-router-dom'

function OutletWrapper() {
  return <Outlet />
}

export function PageCard({ children, title }: { children: ReactNode; title: string }) {
  return (
    <div className="bg-white rounded-2xl shadow-sm p-6">
      <h1 className="text-2xl font-bold text-green-800 mb-6">{title}</h1>
      {children}
    </div>
  )
}
