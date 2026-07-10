import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { Check, Crown, Loader2 } from 'lucide-react'
import { fetchPlans, fetchMySubscription, validatePromoCode } from '../lib/api'
import { PageCard } from '../components/Layout'
import type { Plan, Subscription } from '../types/api'

export function PlansPage() {
  const navigate = useNavigate()
  const location = useLocation() as { state?: { justApplied?: boolean } }
  const [plans, setPlans] = useState<Plan[]>([])
  const [subscription, setSubscription] = useState<Subscription | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [billingCycle, setBillingCycle] = useState<'monthly' | 'annual'>('monthly')
  const [promoCode, setPromoCode] = useState('')
  const [promoMsg, setPromoMsg] = useState('')
  const [discountPct, setDiscountPct] = useState(0)
  const [successMsg, setSuccessMsg] = useState(location.state?.justApplied ? '요금제가 적용되었습니다.' : '')

  const load = () => {
    setLoading(true)
    Promise.all([fetchPlans(), fetchMySubscription()])
      .then(([p, s]) => {
        setPlans(p || [])
        setSubscription(s)
      })
      .catch((err) => setError(err?.response?.data?.msg || err.message || '요금제 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  const handleApplyPromo = async () => {
    setPromoMsg('')
    if (!promoCode.trim()) return
    try {
      const promo = await validatePromoCode(promoCode.trim())
      setDiscountPct(promo.discount_percent || 0)
      setPromoMsg(`프로모션 코드가 적용되었습니다${promo.discount_percent ? ` (${promo.discount_percent}% 할인)` : ''}.`)
    } catch (err: any) {
      setDiscountPct(0)
      setPromoMsg(err?.response?.data?.msg || err.message || '유효하지 않은 코드입니다.')
    }
  }

  const handleSelect = (plan: Plan) => {
    setError('')
    setSuccessMsg('')
    navigate('/checkout', {
      state: { planId: plan.id, billingCycle, promoCode: promoCode.trim() || undefined },
    })
  }


  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <Loader2 className="w-8 h-8 text-green-600 animate-spin" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <PageCard title="요금제">
        {error && <div className="mb-4 p-3 bg-red-50 text-red-700 rounded-lg text-sm">{error}</div>}
        {successMsg && <div className="mb-4 p-3 bg-green-50 text-green-700 rounded-lg text-sm">{successMsg}</div>}

        {subscription && (
          <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-xl flex items-center gap-3">
            <Crown className="w-5 h-5 text-green-700" />
            <div className="text-sm">
              현재 요금제: <span className="font-semibold">{subscription.plan_name || '무료'}</span>{' '}
              {subscription.status === 'active' ? (
                <span className="text-green-700">(사용중)</span>
              ) : (
                <span className="text-red-600">({subscription.status})</span>
              )}
              {subscription.expiry_date && <span className="text-gray-500"> · 만료일 {subscription.expiry_date}</span>}
            </div>
          </div>
        )}

        <div className="flex items-center gap-2 mb-4">
          <button
            onClick={() => setBillingCycle('monthly')}
            className={`px-4 py-2 rounded-lg text-sm font-medium ${
              billingCycle === 'monthly' ? 'bg-green-600 text-white' : 'bg-gray-100 text-gray-600'
            }`}
          >
            월간 결제
          </button>
          <button
            onClick={() => setBillingCycle('annual')}
            className={`px-4 py-2 rounded-lg text-sm font-medium ${
              billingCycle === 'annual' ? 'bg-green-600 text-white' : 'bg-gray-100 text-gray-600'
            }`}
          >
            연간 결제 (2개월 무료 효과)
          </button>
        </div>

        <div className="flex items-center gap-2 mb-6">
          <input
            value={promoCode}
            onChange={(e) => setPromoCode(e.target.value)}
            placeholder="프로모션 코드 입력"
            className="px-3 py-2 border rounded-lg text-sm w-52"
          />
          <button onClick={handleApplyPromo} className="px-3 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg text-sm">
            코드 적용
          </button>
          {promoMsg && <span className="text-xs text-gray-600">{promoMsg}</span>}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {plans.map((plan) => {
            const isCurrent = subscription?.plan_id === plan.id && subscription.status === 'active'
            const price = billingCycle === 'annual' ? plan.price_annual : plan.price_monthly
            const discounted = discountPct > 0 ? Math.round(price * (1 - discountPct / 100)) : price
            return (
              <div
                key={plan.id}
                className={`rounded-2xl border p-5 flex flex-col ${
                  isCurrent ? 'border-green-500 ring-2 ring-green-200' : 'border-gray-200'
                }`}
              >
                <div className="font-bold text-lg text-gray-900">{plan.name}</div>
                <div className="mt-2 mb-4">
                  {discountPct > 0 && discounted !== price && (
                    <div className="text-sm text-gray-400 line-through">{price.toLocaleString()}원</div>
                  )}
                  <div className="text-2xl font-bold text-green-700">
                    {discounted === 0 ? '무료' : `${discounted.toLocaleString()}원`}
                  </div>
                  {discounted !== 0 && (
                    <div className="text-xs text-gray-500">{billingCycle === 'annual' ? '/ 년' : '/ 월'}</div>
                  )}
                </div>
                <ul className="space-y-2 mb-6 flex-1">
                  {plan.features.map((f, i) => (
                    <li key={i} className="flex items-start gap-2 text-sm text-gray-700">
                      <Check className="w-4 h-4 text-green-600 mt-0.5 shrink-0" />
                      {f}
                    </li>
                  ))}
                </ul>
                <button
                  disabled={isCurrent}
                  onClick={() => handleSelect(plan)}
                  className={`w-full py-2 rounded-lg text-sm font-medium transition ${
                    isCurrent
                      ? 'bg-green-100 text-green-700 cursor-default'
                      : 'bg-green-600 hover:bg-green-700 text-white disabled:opacity-60'
                  }`}
                >
                  {isCurrent ? '현재 요금제' : '이 요금제 적용'}
                </button>
              </div>
            )
          })}
        </div>
      </PageCard>
    </div>
  )
}
