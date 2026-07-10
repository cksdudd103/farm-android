import { useEffect, useRef, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { Loader2, ArrowLeft } from 'lucide-react'
import { preparePayment } from '../lib/api'
import { PageCard } from '../components/Layout'

declare global {
  interface Window {
    TossPayments?: (clientKey: string) => any
  }
}

const TOSS_CLIENT_KEY = import.meta.env.VITE_TOSS_CLIENT_KEY || ''
const TOSS_SCRIPT_URL = 'https://js.tosspayments.com/v2/standard'

function loadTossScript(): Promise<void> {
  return new Promise((resolve, reject) => {
    if (window.TossPayments) return resolve()
    const existing = document.querySelector(`script[src="${TOSS_SCRIPT_URL}"]`)
    if (existing) {
      existing.addEventListener('load', () => resolve())
      existing.addEventListener('error', () => reject(new Error('결제 스크립트 로드 실패')))
      return
    }
    const script = document.createElement('script')
    script.src = TOSS_SCRIPT_URL
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('결제 스크립트 로드 실패'))
    document.head.appendChild(script)
  })
}

export function CheckoutPage() {
  const location = useLocation() as { state?: { planId?: number; billingCycle?: 'monthly' | 'annual'; promoCode?: string } }
  const navigate = useNavigate()
  const widgetsRef = useRef<any>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [orderInfo, setOrderInfo] = useState<{ orderId: string; orderName: string; amount: number } | null>(null)

  const planId = location.state?.planId
  const billingCycle = location.state?.billingCycle || 'monthly'
  const promoCode = location.state?.promoCode

  useEffect(() => {
    if (!planId) {
      navigate('/plans', { replace: true })
      return
    }
    let cancelled = false

    const run = async () => {
      try {
        const prep = await preparePayment({ plan_id: planId, billing_cycle: billingCycle, promo_code: promoCode })
        if (cancelled) return

        if (prep.free) {
          navigate('/plans', { replace: true, state: { justApplied: true } })
          return
        }

        if (!TOSS_CLIENT_KEY) {
          setError('결제 시스템이 아직 설정되지 않았습니다 (TOSS 클라이언트 키 미설정). 관리자에게 문의하세요.')
          setLoading(false)
          return
        }

        await loadTossScript()
        if (cancelled) return

        const tossPayments = window.TossPayments!(TOSS_CLIENT_KEY)
        const widgets = tossPayments.widgets({ customerKey: `user_${prep.customer_email || 'guest'}` })
        widgetsRef.current = widgets

        await widgets.setAmount({ currency: 'KRW', value: prep.amount! })
        await Promise.all([
          widgets.renderPaymentMethods({ selector: '#toss-payment-method', variantKey: 'DEFAULT' }),
          widgets.renderAgreement({ selector: '#toss-agreement', variantKey: 'AGREEMENT' }),
        ])

        setOrderInfo({ orderId: prep.order_id!, orderName: prep.order_name!, amount: prep.amount! })
        setLoading(false)
      } catch (err: any) {
        setError(err?.response?.data?.msg || err.message || '결제 준비 중 오류가 발생했습니다.')
        setLoading(false)
      }
    }

    run()
    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [planId])

  const handlePay = async () => {
    if (!widgetsRef.current || !orderInfo) return
    setError('')
    try {
      await widgetsRef.current.requestPayment({
        orderId: orderInfo.orderId,
        orderName: orderInfo.orderName,
        successUrl: `${window.location.origin}/payments/success`,
        failUrl: `${window.location.origin}/payments/fail`,
      })
    } catch (err: any) {
      if (err?.code !== 'USER_CANCEL') {
        setError(err?.message || '결제 요청 중 오류가 발생했습니다.')
      }
    }
  }

  return (
    <div className="space-y-4">
      <button
        onClick={() => navigate('/plans')}
        className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700"
      >
        <ArrowLeft className="w-4 h-4" /> 요금제로 돌아가기
      </button>

      <PageCard title="결제하기">
        {error && <div className="mb-4 p-3 bg-red-50 text-red-700 rounded-lg text-sm whitespace-pre-line">{error}</div>}

        {loading && !error ? (
          <div className="flex items-center justify-center py-16">
            <Loader2 className="w-8 h-8 text-green-600 animate-spin" />
          </div>
        ) : (
          !error && (
            <div className="space-y-4">
              {orderInfo && (
                <div className="p-3 bg-gray-50 rounded-lg text-sm text-gray-700">
                  {orderInfo.orderName} · <span className="font-semibold">{orderInfo.amount.toLocaleString()}원</span>
                </div>
              )}
              <div id="toss-payment-method" />
              <div id="toss-agreement" />
              <button
                onClick={handlePay}
                className="w-full py-3 bg-green-600 hover:bg-green-700 text-white rounded-xl font-medium"
              >
                결제하기
              </button>
            </div>
          )
        )}
      </PageCard>
    </div>
  )
}
