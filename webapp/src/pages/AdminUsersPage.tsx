import { useEffect, useMemo, useState } from 'react'
import { fetchUsers, updateUser, deleteUser } from '../lib/api'
import { PageCard } from '../components/Layout'
import { useAuth } from '../contexts/AuthContext'
import { Users, UserCheck, UserX, Search } from 'lucide-react'
import type { User } from '../types/api'

export function AdminUsersPage() {
  const { user } = useAuth()
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [search, setSearch] = useState('')
  const [roleFilter, setRoleFilter] = useState<'all' | 'admin' | 'farmer'>('all')
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all')

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

  const filteredUsers = useMemo(() => {
    return users.filter((u) => {
      const matchesSearch =
        !search ||
        u.name.toLowerCase().includes(search.toLowerCase()) ||
        u.email.toLowerCase().includes(search.toLowerCase())
      const matchesRole = roleFilter === 'all' || u.role === roleFilter
      const matchesStatus =
        statusFilter === 'all' ||
        (statusFilter === 'active' ? u.is_active_user !== false : u.is_active_user === false)
      return matchesSearch && matchesRole && matchesStatus
    })
  }, [users, search, roleFilter, statusFilter])

  const stats = useMemo(() => {
    const total = users.length
    const admins = users.filter((u) => u.role === 'admin').length
    const farmers = users.filter((u) => u.role === 'farmer').length
    const active = users.filter((u) => u.is_active_user !== false).length
    const inactive = users.filter((u) => u.is_active_user === false).length
    return { total, admins, farmers, active, inactive }
  }, [users])

  if (user?.role !== 'admin') {
    return <PageCard title="회원 관리"><p className="text-red-600">관리자 권한이 필요합니다.</p></PageCard>
  }

  const toggleRole = async (target: User) => {
    const newRole = target.role === 'admin' ? 'farmer' : 'admin'
    try {
      await updateUser(target.id, { role: newRole })
      setMessage(`'${target.name}' 님의 권한을 ${newRole === 'admin' ? '관리자' : '농민'}로 변경했습니다.`)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '권한 변경에 실패했습니다.')
    }
  }

  const toggleActive = async (target: User) => {
    try {
      await updateUser(target.id, { is_active_user: !target.is_active_user })
      setMessage(`'${target.name}' 님을 ${!target.is_active_user ? '활성화' : '비활성화'}했습니다.`)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '상태 변경에 실패했습니다.')
    }
  }

  const handleDelete = async (target: User) => {
    if (!confirm(`'${target.name}' 님을 탈퇴 처리하시겠습니까?\n이 작업은 되돌릴 수 없습니다.`)) return
    try {
      await deleteUser(target.id)
      setMessage(`'${target.name}' 님을 탈퇴 처리했습니다.`)
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
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <StatCard icon={Users} label="총 회원" value={stats.total} color="bg-blue-50 text-blue-700" />
        <StatCard icon={UserCheck} label="관리자" value={stats.admins} color="bg-purple-50 text-purple-700" />
        <StatCard icon={UserCheck} label="농민" value={stats.farmers} color="bg-green-50 text-green-700" />
        <StatCard icon={UserX} label="비활성" value={stats.inactive} color="bg-red-50 text-red-700" />
      </div>

      <div className="flex flex-col md:flex-row gap-3 mb-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="이름 또는 이메일 검색"
            className="w-full pl-9 pr-3 py-2 border rounded-lg"
          />
        </div>
        <select
          value={roleFilter}
          onChange={(e) => setRoleFilter(e.target.value as any)}
          className="px-3 py-2 border rounded-lg"
        >
          <option value="all">전체 권한</option>
          <option value="admin">관리자</option>
          <option value="farmer">농민</option>
        </select>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as any)}
          className="px-3 py-2 border rounded-lg"
        >
          <option value="all">전체 상태</option>
          <option value="active">정상</option>
          <option value="inactive">비활성</option>
        </select>
      </div>

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

      <p className="text-sm text-gray-500 mb-2">총 {filteredUsers.length}명 표시 중</p>

      {filteredUsers.length === 0 ? (
        <p className="text-gray-500">조건에 맞는 회원이 없습니다.</p>
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
              {filteredUsers.map((target) => (
                <tr key={target.id} className={!target.is_active_user ? 'bg-gray-100 text-gray-500' : ''}>
                  <td className="px-4 py-3">{target.id}</td>
                  <td className="px-4 py-3 font-medium">{target.name}</td>
                  <td className="px-4 py-3">{target.email}</td>
                  <td className="px-4 py-3">
                    <span className={`text-xs px-2 py-1 rounded ${target.role === 'admin' ? 'bg-purple-100 text-purple-700' : 'bg-gray-100 text-gray-700'}`}>
                      {target.role === 'admin' ? '관리자' : '농민'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`text-xs px-2 py-1 rounded ${target.is_active_user !== false ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                      {target.is_active_user !== false ? '정상' : '비활성'}
                    </span>
                  </td>
                  <td className="px-4 py-3">{target.created_at || '-'}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2 flex-wrap">
                      <button
                        onClick={() => toggleRole(target)}
                        className="text-xs px-2 py-1 border rounded hover:bg-gray-50"
                      >
                        {target.role === 'admin' ? '관리자 해제' : '관리자 지정'}
                      </button>
                      <button
                        onClick={() => toggleActive(target)}
                        className={`text-xs px-2 py-1 border rounded ${target.is_active_user !== false ? 'text-red-600 hover:bg-red-50' : 'text-green-600 hover:bg-green-50'}`}
                      >
                        {target.is_active_user !== false ? '비활성화' : '활성화'}
                      </button>
                      <button
                        onClick={() => handleDelete(target)}
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

function StatCard({
  icon: Icon,
  label,
  value,
  color,
}: {
  icon: React.ElementType
  label: string
  value: number
  color: string
}) {
  return (
    <div className={`rounded-xl p-4 flex items-center gap-3 ${color}`}>
      <Icon className="w-6 h-6" />
      <div>
        <p className="text-2xl font-bold">{value}</p>
        <p className="text-sm opacity-80">{label}</p>
      </div>
    </div>
  )
}
