import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { Sprout, Eye, EyeOff } from 'lucide-react'

export function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [rememberMe, setRememberMe] = useState(true)
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [isRegisterOpen, setIsRegisterOpen] = useState(false)

  if (user) return <div className="min-h-screen flex items-center justify-center"><div className="w-10 h-10 border-4 border-green-600 border-t-transparent rounded-full animate-spin" /></div>

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      await login(email, password, rememberMe)
      navigate('/')
    } catch (err: any) {
      setError(err.response?.data?.error || '로그인에 실패했습니다.')
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl shadow-xl w-full max-w-md p-8">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-green-100 mb-4">
            <Sprout className="w-8 h-8 text-green-600" />
          </div>
          <h1 className="text-2xl font-bold text-green-800">스마트영농</h1>
          <p className="text-gray-500 mt-1">농업인을 위한 영농 관리 서비스</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">이메일</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-3 rounded-xl border border-gray-300 focus:border-green-500 focus:ring-2 focus:ring-green-200 outline-none transition"
              placeholder="email@example.com"
              required
            />
          </div>

          <div className="relative">
            <label className="block text-sm font-medium text-gray-700 mb-1">비밀번호</label>
            <input
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-3 rounded-xl border border-gray-300 focus:border-green-500 focus:ring-2 focus:ring-green-200 outline-none transition"
              placeholder="비밀번호 입력"
              required
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-[34px] text-gray-400 hover:text-gray-600"
            >
              {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
            </button>
          </div>

          <div className="flex items-center justify-between text-sm">
            <label className="flex items-center gap-2 text-gray-700">
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                className="w-4 h-4 text-green-600 rounded focus:ring-green-500"
              />
              로그인 상태 유지
            </label>
          </div>

          {error && (
            <p className="text-red-600 text-sm text-center">{error}</p>
          )}

          <button
            type="submit"
            className="w-full py-3.5 bg-green-700 hover:bg-green-800 text-white font-semibold rounded-2xl transition"
          >
            로그인
          </button>
        </form>

        <div className="mt-6 text-center">
          <button
            onClick={() => setIsRegisterOpen(true)}
            className="text-green-700 font-medium hover:underline"
          >
            아직 계정이 없으신가요? 회원가입
          </button>
        </div>
      </div>

      {isRegisterOpen && <RegisterModal onClose={() => setIsRegisterOpen(false)} />}
    </div>
  )
}

function RegisterModal({ onClose }: { onClose: () => void }) {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [error, setError] = useState('')

  const [adminCode, setAdminCode] = useState('')
  const [showAdminCode, setShowAdminCode] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      await register(name, email, password, passwordConfirm, adminCode.trim() || undefined)
      navigate('/')
    } catch (err: any) {
      const message =
        err?.response?.data?.error ||
        err?.response?.data?.message ||
        err?.message ||
        '회원가입에 실패했습니다.'
      console.error('Register error:', err?.response?.data ?? err)
      setError(message)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40">
      <div className="bg-white rounded-3xl shadow-2xl w-full max-w-md p-8">
        <h2 className="text-2xl font-bold text-gray-900 mb-6">회원가입</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            type="text"
            placeholder="이름"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full px-4 py-3 rounded-xl border border-gray-300 focus:border-green-500 focus:ring-2 focus:ring-green-200 outline-none"
            required
          />
          <input
            type="email"
            placeholder="이메일"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full px-4 py-3 rounded-xl border border-gray-300 focus:border-green-500 focus:ring-2 focus:ring-green-200 outline-none"
            required
          />
          <input
            type="password"
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full px-4 py-3 rounded-xl border border-gray-300 focus:border-green-500 focus:ring-2 focus:ring-green-200 outline-none"
            required
          />
          <input
            type="password"
            placeholder="비밀번호 확인"
            value={passwordConfirm}
            onChange={(e) => setPasswordConfirm(e.target.value)}
            className="w-full px-4 py-3 rounded-xl border border-gray-300 focus:border-green-500 focus:ring-2 focus:ring-green-200 outline-none"
            required
          />

          <div className="border-t border-gray-200 pt-4">
            <label className="flex items-center gap-2 text-sm text-gray-700 mb-2">
              <input
                type="checkbox"
                checked={showAdminCode}
                onChange={(e) => setShowAdminCode(e.target.checked)}
                className="w-4 h-4 text-green-600 rounded focus:ring-green-500"
              />
              관리자 코드 입력
            </label>
            {showAdminCode && (
              <input
                type="text"
                placeholder="관리자 코드"
                value={adminCode}
                onChange={(e) => setAdminCode(e.target.value)}
                className="w-full px-4 py-3 rounded-xl border border-gray-300 focus:border-green-500 focus:ring-2 focus:ring-green-200 outline-none"
              />
            )}
          </div>

          {error && <p className="text-red-600 text-sm">{error}</p>}
          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-3 rounded-xl border border-gray-300 text-gray-700 font-medium hover:bg-gray-50"
            >
              취소
            </button>
            <button
              type="submit"
              className="flex-1 py-3 rounded-xl bg-green-700 text-white font-medium hover:bg-green-800"
            >
              가입하기
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
