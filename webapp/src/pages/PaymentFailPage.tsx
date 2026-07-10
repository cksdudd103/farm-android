import { useNavigate, useSearchParams } from 'react-router-dom'
import { XCircle } from 'lucide-react'
import { PageCard } from '../components/Layout'

export function PaymentFailPage() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const message = params.get('message') || '결제가 취소되었거나 실패했습니다.'

  return (
    <PageCard title="결제 결과">
      <div className="flex flex-col items-center justify-center py-16 gap-3">
        <XCircle className="w-12 h-12 text-red-500" />
        <div className="text-lg font-semibold text-red-600">결제에 실패했습니다.</div>
        <div className="text-sm text-gray-500">{message}</div>
        <button
          onClick={() => navigate('/plans')}
          className="mt-4 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg text-sm"
        >
          요금제 페이지로 돌아가기
        </button>
      </div>
    </PageCard>
  )
}
