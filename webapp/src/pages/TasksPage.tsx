import { useEffect, useState } from 'react'
import {
  fetchTasks,
  createTask,
  updateTask,
  deleteTask,
  fetchCrops,
} from '../lib/api'
import { PageCard } from '../components/Layout'
import type { Task, Crop } from '../types/api'

const priorities = ['높음', '보통', '낮음']
const statuses = ['예정', '진행중', '완료']

export function TasksPage() {
  const [tasks, setTasks] = useState<Task[]>([])
  const [crops, setCrops] = useState<Crop[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState<Task | null>(null)
  const [isOpen, setIsOpen] = useState(false)

  const load = () => {
    setLoading(true)
    Promise.all([fetchTasks(), fetchCrops()])
      .then(([t, c]) => {
        setTasks(t || [])
        setCrops(c || [])
      })
      .catch((err) => setError(err?.response?.data?.msg || err.message || '데이터를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError('')
    const form = e.currentTarget
    const fd = new FormData(form)
    const payload: Partial<Task> = {
      title: fd.get('title') as string,
      memo: (fd.get('memo') as string) || undefined,
      due_date: (fd.get('due_date') as string) || undefined,
      priority: fd.get('priority') as string,
      status: fd.get('status') as string,
      crop_id: fd.get('crop_id') ? Number(fd.get('crop_id')) : undefined,
    }
    try {
      if (editing) {
        await updateTask(editing.id, payload)
      } else {
        await createTask(payload as Omit<Task, 'id' | 'user_id' | 'created_at'>)
      }
      setIsOpen(false)
      setEditing(null)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '저장에 실패했습니다.')
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('삭제하시겠습니까?')) return
    try {
      await deleteTask(id)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '삭제에 실패했습니다.')
    }
  }

  if (loading) return <PageCard title="작업 일정"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="작업 일정">
      <div className="flex justify-end mb-4">
        <button
          onClick={() => { setEditing(null); setIsOpen(true) }}
          className="px-4 py-2 bg-green-700 text-white rounded-lg hover:bg-green-800"
        >
          작업 등록
        </button>
      </div>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      {tasks.length === 0 ? (
        <p className="text-gray-500">등록된 작업이 없습니다.</p>
      ) : (
        <ul className="divide-y divide-gray-200">
          {tasks.map((t) => (
            <li key={t.id} className="py-4 flex justify-between items-start">
              <div>
                <div className="flex items-center gap-2">
                  <p className="font-semibold text-gray-900">{t.title}</p>
                  <span className={`text-xs px-1.5 py-0.5 rounded ${priorityClass(t.priority)}`}>{t.priority}</span>
                  <span className={`text-xs px-1.5 py-0.5 rounded ${statusClass(t.status)}`}>{t.status}</span>
                </div>
                <p className="text-sm text-gray-500">
                  {t.crop_name || '-'} · {t.due_date || '마감일 없음'}
                </p>
                {t.memo && <p className="text-sm text-gray-600 mt-1">{t.memo}</p>}
              </div>
              <div className="flex gap-2">
                <button onClick={() => { setEditing(t); setIsOpen(true) }} className="text-sm text-green-700 hover:underline">수정</button>
                <button onClick={() => handleDelete(t.id)} className="text-sm text-red-600 hover:underline">삭제</button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {isOpen && (
        <TaskModal
          task={editing}
          crops={crops}
          onClose={() => { setIsOpen(false); setEditing(null) }}
          onSubmit={handleSubmit}
        />
      )}
    </PageCard>
  )
}

function TaskModal({
  task,
  crops,
  onClose,
  onSubmit,
}: {
  task: Task | null
  crops: Crop[]
  onClose: () => void
  onSubmit: (e: React.FormEvent<HTMLFormElement>) => void
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6">
        <h2 className="text-xl font-bold mb-4">{task ? '작업 수정' : '작업 등록'}</h2>
        <form onSubmit={onSubmit} className="space-y-3">
          <input name="title" defaultValue={task?.title} placeholder="작업명" className="w-full px-3 py-2 border rounded-lg" required />
          <select name="crop_id" defaultValue={task?.crop_id || ''} className="w-full px-3 py-2 border rounded-lg">
            <option value="">작물 선택</option>
            {crops.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
          <input name="due_date" type="date" defaultValue={task?.due_date || ''} placeholder="마감일" className="w-full px-3 py-2 border rounded-lg" />
          <select name="priority" defaultValue={task?.priority || '보통'} className="w-full px-3 py-2 border rounded-lg">
            {priorities.map((p) => <option key={p} value={p}>{p}</option>)}
          </select>
          <select name="status" defaultValue={task?.status || '예정'} className="w-full px-3 py-2 border rounded-lg">
            {statuses.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <textarea name="memo" defaultValue={task?.memo || ''} placeholder="메모" className="w-full px-3 py-2 border rounded-lg" rows={3} />

          <div className="flex gap-2 pt-2">
            <button type="button" onClick={onClose} className="flex-1 py-2 border rounded-lg">취소</button>
            <button type="submit" className="flex-1 py-2 bg-green-700 text-white rounded-lg">저장</button>
          </div>
        </form>
      </div>
    </div>
  )
}

function priorityClass(priority: string) {
  switch (priority) {
    case '높음': return 'bg-red-100 text-red-700'
    case '낮음': return 'bg-blue-100 text-blue-700'
    default: return 'bg-gray-100 text-gray-700'
  }
}

function statusClass(status: string) {
  switch (status) {
    case '완료': return 'bg-green-100 text-green-700'
    case '진행중': return 'bg-yellow-100 text-yellow-700'
    default: return 'bg-gray-100 text-gray-700'
  }
}
