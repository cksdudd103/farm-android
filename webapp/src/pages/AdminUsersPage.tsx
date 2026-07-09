import { useEffect, useState } from 'react'
import { fetchUsers, updateUser, deleteUser } from '../lib/api'
import { PageCard } from '../components/Layout'
import type { User } from '../types/api'

import { useAuth } from '../contexts/AuthContext'

export function AdminUsersPage() {
  const { user } = useAuth()
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  if (user?.role !== 'admin') {
    return <PageCard title="회원 관리"><p className="text-red-600">관리자 권한이 필요합니다.</p></PageCard>
  }

  const load = () => {
    setLoading(true)
    fetchUsers()
      .then((data) => setUsers(data || []))
      .catch((err) => setError(err?.response?.data?.msg || err.message || '회원 목록을 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  const toggleRole = async (user: User) => {
    const newRole = user.role === 'admin' ? 'farmer' : 'admin'
    try {
      await updateUser(user.id, { role: newRole })
      setMessage(`'${user.name}' 님의 권한을 ${newRole === 'admin' ? '관리자' : '농민'}로 변경했습니다.`)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '권한 변경에 실패했습니다.')
    }
  }

  const toggleActive = async (user: User) => {
    try {
      await updateUser(user.id, { is_active_user: !user.is_active_user })
      setMessage(`'${user.name}' 님을 ${!user.is_active_user ? '활성화' : '비활성화'}했습니다.`)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '상태 변경에 실패했습니다.')
    }
  }

  const handleDelete = async (user: User) => {
    if (!confirm(`'${user.name}' 님을 탈퇴 처리하시겠습니까?\n이 작업은 되돌릴 수 없습니다.`)) return
    try {
      await deleteUser(user.id)
      setMessage(`'${user.name}' 님을 탈퇴 처리했습니다.`)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '탈퇴 처리에 실패했습니다.')
    }
  }

  const handleResetAll = async () => {
    const others = users.filter((u) => u.id !== user?.id)
    if (others.length === 0) {
      setMessage('삭제할 다른 회원이 없습니다.')
      return
    }
    if (!confirm(`총 ${others.length}명의 회원을 탈퇴 처리하시겠습니까?\n본인은 제외됩니다.\n이 작업은 되돌릴 수 없습니다.`)) return
    let deleted = 0
    for (const u of others) {
      try {
        await deleteUser(u.id)
        deleted++
      } catch (e) {
        // ignore individual failures
      }
    }
    setMessage(`${deleted}명의 회원을 탈퇴 처리했습니다.`)
    load()
  }

  if (loading) return <PageCard title="회원 관리"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="회원 관리">
      <div className="flex justify-end gap-2 mb-4">
        <button
          onClick={handleResetAll}
          className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
        >
          회원 목록 초기화
        </button>
      </div>

      {message && <div className="mb-4 p-3 bg-green-50 text-green-800 rounded-lg">{message}</div>}
      {error && <div className="mb-4 p-3 bg-red-50 text-red-700 rounded-lg">{error}</div>}

      {users.length === 0 ? (
        <p className="text-gray-500">등록된 회원이 없습니다.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-green-50">
              <tr>
                <th className="text-left px-4 py-3">ID</th>
                <th className="text-left px-4 py-3">이름</th>
                <th className="text-left px-4 py-3">이메일</th>
                <th className="text-left px-4 py-3">권한</th>
                <th className="text-left px-4 py-3">상태</th>
                <th className="text-left px-4 py-3">가입일</th>
                <th className="text-left px-4 py-3">관리</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {users.map((user) => (
                <tr key={user.id} className={!user.is_active_user ? 'bg-gray-100 text-gray-500' : ''}>
                  <td className="px-4 py-3">{user.id}</td>
                  <td className="px-4 py-3 font-medium">{user.name}</td>
                  <td className="px-4 py-3">{user.email}</td>
                  <td className="px-4 py-3">
                    <span className={`text-xs px-2 py-1 rounded ${user.role === 'admin' ? 'bg-purple-100 text-purple-700' : 'bg-gray-100 text-gray-700'}`}>
                      {user.role === 'admin' ? '관리자' : '농민'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`text-xs px-2 py-1 rounded ${user.is_active_user !== false ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                      {user.is_active_user !== false ? '정상' : '비활성'}
                    </span>
                  </td>
                  <td className="px-4 py-3">{user.created_at || '-'}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      <button
                        onClick={() => toggleRole(user)}
                        className="text-xs px-2 py-1 border rounded hover:bg-gray-50"
                      >
                        {user.role === 'admin' ? '관리자 해제' : '관리자 지정'}
                      </button>
                      <button
                        onClick={() => toggleActive(user)}
                        className={`text-xs px-2 py-1 border rounded ${user.is_active_user !== false ? 'text-red-600 hover:bg-red-50' : 'text-green-600 hover:bg-green-50'}`}
                      >
                        {user.is_active_user !== false ? '비활성화' : '활성화'}
                      </button>
                      <button
                        onClick={() => handleDelete(user)}
                        className="text-xs px-2 py-1 border rounded text-red-600 hover:bg-red-50"
                      >
                        탈퇴
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </PageCard>
  )
}
