import { useEffect, useState } from 'react'
import {
  fetchInventory,
  createInventory,
  updateInventory,
  deleteInventory,
} from '../lib/api'
import { PageCard } from '../components/Layout'
import type { InventoryItem } from '../types/api'

const categories = ['종자', '비료', '농약', '농자재', '기타']

export function InventoryPage() {
  const [items, setItems] = useState<InventoryItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState<InventoryItem | null>(null)
  const [isOpen, setIsOpen] = useState(false)

  const load = () => {
    setLoading(true)
    fetchInventory()
      .then((data) => setItems(data || []))
      .catch((err) => setError(err?.response?.data?.msg || err.message || '재고 목록을 불러오지 못했습니다.'))
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
    const payload: Partial<InventoryItem> = {
      name: fd.get('name') as string,
      category: (fd.get('category') as string) || undefined,
      quantity: Number(fd.get('quantity')),
      unit: (fd.get('unit') as string) || undefined,
      location: (fd.get('location') as string) || undefined,
      expiry_date: (fd.get('expiry_date') as string) || undefined,
      memo: (fd.get('memo') as string) || undefined,
    }
    try {
      if (editing) {
        await updateInventory(editing.id, payload)
      } else {
        await createInventory(payload as Omit<InventoryItem, 'id' | 'user_id' | 'created_at'>)
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
      await deleteInventory(id)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '삭제에 실패했습니다.')
    }
  }

  if (loading) return <PageCard title="재고 관리"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="재고 관리">
      <div className="flex justify-end mb-4">
        <button
          onClick={() => { setEditing(null); setIsOpen(true) }}
          className="px-4 py-2 bg-green-700 text-white rounded-lg hover:bg-green-800"
        >
          재고 등록
        </button>
      </div>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      {items.length === 0 ? (
        <p className="text-gray-500">등록된 재고가 없습니다.</p>
      ) : (
        <ul className="divide-y divide-gray-200">
          {items.map((item) => (
            <li key={item.id} className="py-4 flex justify-between items-start">
              <div>
                <p className="font-semibold text-gray-900">{item.name}</p>
                <p className="text-sm text-gray-500">
                  {item.category || '-'} · {item.quantity} {item.unit || ''} · {item.location || '위치 미정'}
                </p>
                {item.expiry_date && <p className="text-sm text-gray-500">유통기한: {item.expiry_date}</p>}
                {item.memo && <p className="text-sm text-gray-600 mt-1">{item.memo}</p>}
              </div>
              <div className="flex gap-2">
                <button onClick={() => { setEditing(item); setIsOpen(true) }} className="text-sm text-green-700 hover:underline">수정</button>
                <button onClick={() => handleDelete(item.id)} className="text-sm text-red-600 hover:underline">삭제</button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {isOpen && (
        <InventoryModal
          item={editing}
          onClose={() => { setIsOpen(false); setEditing(null) }}
          onSubmit={handleSubmit}
        />
      )}
    </PageCard>
  )
}

function InventoryModal({
  item,
  onClose,
  onSubmit,
}: {
  item: InventoryItem | null
  onClose: () => void
  onSubmit: (e: React.FormEvent<HTMLFormElement>) => void
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6">
        <h2 className="text-xl font-bold mb-4">{item ? '재고 수정' : '재고 등록'}</h2>
        <form onSubmit={onSubmit} className="space-y-3">
          <input name="name" defaultValue={item?.name} placeholder="품목명" className="w-full px-3 py-2 border rounded-lg" required />
          <select name="category" defaultValue={item?.category || ''} className="w-full px-3 py-2 border rounded-lg">
            <option value="">분류 선택</option>
            {categories.map((c) => <option key={c} value={c}>{c}</option>)}
          </select>
          <div className="flex gap-2">
            <input name="quantity" type="number" step="0.01" defaultValue={item?.quantity} placeholder="수량" className="flex-1 px-3 py-2 border rounded-lg" required />
            <input name="unit" defaultValue={item?.unit || ''} placeholder="단위" className="w-24 px-3 py-2 border rounded-lg" />
          </div>
          <input name="location" defaultValue={item?.location || ''} placeholder="보관 위치" className="w-full px-3 py-2 border rounded-lg" />
          <input name="expiry_date" type="date" defaultValue={item?.expiry_date || ''} placeholder="유통기한" className="w-full px-3 py-2 border rounded-lg" />
          <textarea name="memo" defaultValue={item?.memo || ''} placeholder="메모" className="w-full px-3 py-2 border rounded-lg" rows={3} />

          <div className="flex gap-2 pt-2">
            <button type="button" onClick={onClose} className="flex-1 py-2 border rounded-lg">취소</button>
            <button type="submit" className="flex-1 py-2 bg-green-700 text-white rounded-lg">저장</button>
          </div>
        </form>
      </div>
    </div>
  )
}
