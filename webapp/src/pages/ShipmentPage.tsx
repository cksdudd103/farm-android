import { useEffect, useMemo, useState } from 'react'
import {
  fetchShipments,
  createShipment,
  updateShipment,
  deleteShipment,
  fetchCrops,
} from '../lib/api'
import { PageCard } from '../components/Layout'
import type { Shipment, Crop } from '../types/api'

const statuses = ['예정', '출하완료', '정산완료']

export function ShipmentPage() {
  const [shipments, setShipments] = useState<Shipment[]>([])
  const [crops, setCrops] = useState<Crop[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState<Shipment | null>(null)
  const [isOpen, setIsOpen] = useState(false)

  const load = () => {
    setLoading(true)
    Promise.all([fetchShipments(), fetchCrops().catch(() => [])])
      .then(([s, c]) => {
        setShipments(s || [])
        setCrops(c || [])
      })
      .catch((err) => setError(err?.response?.data?.msg || err.message || '출하 목록을 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  const totalAmount = useMemo(
    () => shipments.reduce((sum, s) => sum + (s.total_price || (s.quantity || 0) * (s.unit_price || 0)), 0),
    [shipments]
  )

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError('')
    const fd = new FormData(e.currentTarget)
    const payload: Partial<Shipment> = {
      crop_id: fd.get('crop_id') ? Number(fd.get('crop_id')) : undefined,
      buyer: (fd.get('buyer') as string) || undefined,
      quantity: fd.get('quantity') ? Number(fd.get('quantity')) : 0,
      unit: (fd.get('unit') as string) || undefined,
      unit_price: fd.get('unit_price') ? Number(fd.get('unit_price')) : 0,
      shipment_date: (fd.get('shipment_date') as string) || undefined,
      status: (fd.get('status') as string) || '예정',
      memo: (fd.get('memo') as string) || undefined,
    }

    try {
      if (editing) {
        await updateShipment(editing.id, payload)
      } else {
        await createShipment(payload)
      }
      setIsOpen(false)
      setEditing(null)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '저장에 실패했습니다.')
    }
  }

  const handleDelete = async (id: number) => {
    if (!id) return
    if (!confirm('삭제하시겠습니까?')) return
    try {
      await deleteShipment(id)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '삭제에 실패했습니다.')
    }
  }

  const openNew = () => {
    setEditing(null)
    setIsOpen(true)
  }

  const openEdit = (s: Shipment) => {
    setEditing(s)
    setIsOpen(true)
  }

  if (loading) return <PageCard title="출하 관리"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="출하 관리">
      <div className="mb-6 p-4 bg-green-50 rounded-xl border border-green-100 flex justify-between items-center">
        <p className="text-sm text-green-800">총 출하 건수: <span className="font-bold">{shipments.length}건</span></p>
        <p className="text-sm text-green-800">총 출하 금액: <span className="font-bold">{totalAmount.toLocaleString()}원</span></p>
      </div>

      <div className="flex justify-end mb-4">
        <button onClick={openNew} className="px-4 py-2 bg-green-700 text-white rounded-lg hover:bg-green-800">
          출하 등록
        </button>
      </div>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      {shipments.length === 0 ? (
        <p className="text-gray-500">등록된 출하 기록이 없습니다.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-green-50">
              <tr>
                <th className="text-left px-4 py-3 font-semibold text-gray-700">작물</th>
                <th className="text-left px-4 py-3 font-semibold text-gray-700">거래처</th>
                <th className="text-right px-4 py-3 font-semibold text-gray-700">수량</th>
                <th className="text-right px-4 py-3 font-semibold text-gray-700">단가</th>
                <th className="text-right px-4 py-3 font-semibold text-gray-700">총액</th>
                <th className="text-left px-4 py-3 font-semibold text-gray-700">출하일</th>
                <th className="text-left px-4 py-3 font-semibold text-gray-700">상태</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {shipments.map((s) => (
                <tr key={s.id}>
                  <td className="px-4 py-3 font-medium">{s.crop_name || '-'}</td>
                  <td className="px-4 py-3">{s.buyer || '-'}</td>
                  <td className="px-4 py-3 text-right">{s.quantity} {s.unit || ''}</td>
                  <td className="px-4 py-3 text-right">{s.unit_price?.toLocaleString()}원</td>
                  <td className="px-4 py-3 text-right font-semibold">{(s.total_price || s.quantity * s.unit_price).toLocaleString()}원</td>
                  <td className="px-4 py-3">{s.shipment_date || '-'}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs ${
                      s.status === '정산완료' ? 'bg-green-100 text-green-700' :
                      s.status === '출하완료' ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-600'
                    }`}>{s.status}</span>
                  </td>
                  <td className="px-4 py-3 text-right whitespace-nowrap">
                    <button onClick={() => openEdit(s)} className="text-green-700 hover:underline mr-3">수정</button>
                    <button onClick={() => handleDelete(s.id)} className="text-red-600 hover:underline">삭제</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {isOpen && (
        <ShipmentModal
          shipment={editing}
          crops={crops}
          onClose={() => { setIsOpen(false); setEditing(null) }}
          onSubmit={handleSubmit}
        />
      )}
    </PageCard>
  )
}

function ShipmentModal({
  shipment,
  crops,
  onClose,
  onSubmit,
}: {
  shipment: Shipment | null
  crops: Crop[]
  onClose: () => void
  onSubmit: (e: React.FormEvent<HTMLFormElement>) => void
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6 max-h-[90vh] overflow-y-auto">
        <h2 className="text-xl font-bold mb-4">{shipment ? '출하 수정' : '출하 등록'}</h2>
        <form onSubmit={onSubmit} className="space-y-3">
          <select name="crop_id" defaultValue={shipment?.crop_id ?? ''} className="w-full px-3 py-2 border rounded-lg">
            <option value="">작물 선택 (선택 안 함)</option>
            {crops.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
          <input name="buyer" defaultValue={shipment?.buyer || ''} placeholder="거래처" className="w-full px-3 py-2 border rounded-lg" />
          <div className="grid grid-cols-2 gap-2">
            <input name="quantity" type="number" step="0.01" defaultValue={shipment?.quantity ?? ''} placeholder="수량" className="w-full px-3 py-2 border rounded-lg" required />
            <input name="unit" defaultValue={shipment?.unit || ''} placeholder="단위 (kg, box 등)" className="w-full px-3 py-2 border rounded-lg" />
          </div>
          <input name="unit_price" type="number" step="1" defaultValue={shipment?.unit_price ?? ''} placeholder="단가 (원)" className="w-full px-3 py-2 border rounded-lg" required />
          <input name="shipment_date" type="date" defaultValue={shipment?.shipment_date || ''} className="w-full px-3 py-2 border rounded-lg" />
          <select name="status" defaultValue={shipment?.status || '예정'} className="w-full px-3 py-2 border rounded-lg">
            {statuses.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <textarea name="memo" defaultValue={shipment?.memo || ''} placeholder="메모" className="w-full px-3 py-2 border rounded-lg" rows={3} />

          <div className="flex gap-2 pt-2">
            <button type="button" onClick={onClose} className="flex-1 py-2 border rounded-lg">취소</button>
            <button type="submit" className="flex-1 py-2 bg-green-700 text-white rounded-lg">저장</button>
          </div>
        </form>
      </div>
    </div>
  )
}
