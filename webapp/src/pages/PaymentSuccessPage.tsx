import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Loader2, CheckCircle2, XCircle } from 'lucide-react'
import { confirmTossPayment } from '../lib/api'
import { PageCard } from '../components/Layout'
import { useAuth } from '../contexts/AuthContext'

export function PaymentSuccessPage() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const { refreshUser } = useAuth()
  const [status, setStatus] = useState<'loading' | 'done' | 'error'>('loading')
  const [msg, setMsg] = useState('')

  useEffect(() => {
    const paymentKey = params.get('paymentKey')
    const orderId = params.get('orderId')
    const amount = params.get('amount')
    if (!paymentKey || !orderId || !amount) {
      setStatus('error')
      setMsg('결제 정보가 올바르지 않습니다.')
      return
    }
    confirmTossPayment({ paymentKey, orderId, amount: Number(amount) })
      .then(async () => {
        await refreshUser()
        setStatus('done')
      })
      .catch((err) => {
        setStatus('error')
        setMsg(err?.response?.data?.msg || err.message || '결제 승인에 실패했습니다.')
      })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <PageCard title="결제 결과">
      <div className="flex flex-col items-center justify-center py-16 gap-3">
        {status === 'loading' && <Loader2 className="w-10 h-10 text-green-600 animate-spin" />}
        {status === 'done' && (
          <>
            <CheckCircle2 className="w-12 h-12 text-green-600" />
            <div className="text-lg font-semibold">결제가 완료되었습니다.</div>
          </>
        )}
        {status === 'error' && (
          <>
            <XCircle className="w-12 h-12 text-red-500" />
            <div className="text-lg font-semibold text-red-600">결제 승인에 실패했습니다.</div>
            <div className="text-sm text-gray-500">{msg}</div>
          </>
        )}
        <button
          onClick={() => navigate('/plans')}
          className="mt-4 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg text-sm"
        >
          요금제 페이지로 이동
        </button>
      </div>
    </PageCard>
  )
}
