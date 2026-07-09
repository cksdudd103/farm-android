import { useEffect, useMemo, useState } from 'react'
import { fetchWeather, fetchCrops } from '../lib/api'
import { PageCard } from '../components/Layout'
import { CloudSun, CloudRain, Cloud, Sun, Wind, Sprout, MapPin } from 'lucide-react'
import { cropGuides, getCropGuide, getWeatherSuitability, getWeatherAdvice } from '../data/cropGuides'
import type { WeatherResponse, Crop } from '../types/api'

export function WeatherPage() {
  const [weather, setWeather] = useState<WeatherResponse | null>(null)
  const [crops, setCrops] = useState<Crop[]>([])
  const [region, setRegion] = useState('서울')
  const [selectedCrop, setSelectedCrop] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = (r: string) => {
    setLoading(true)
    setError('')
    Promise.all([fetchWeather(r), fetchCrops().catch(() => [])])
      .then(([w, c]) => {
        setWeather(w)
        setCrops(c || [])
      })
      .catch((err) => {
        console.warn('Weather API failed:', err)
        setError('날씨 정보를 불러오지 못했습니다.')
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load(region)
  }, [])

  const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    load(region)
  }

  const todayWeather = useMemo(() => weather?.data?.[0], [weather])
  const guide = useMemo(() => getCropGuide(selectedCrop), [selectedCrop])
  const suitability = useMemo(() => {
    if (!guide || !todayWeather) return null
    return getWeatherSuitability(guide, todayWeather.temp_max, todayWeather.humidity)
  }, [guide, todayWeather])
  const advice = useMemo(() => {
    if (!guide || !todayWeather) return null
    return getWeatherAdvice(guide, todayWeather.temp_max, todayWeather.humidity)
  }, [guide, todayWeather])

  if (loading) return <PageCard title="날씨 예보"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="날씨 예보">
      <form onSubmit={handleSearch} className="flex gap-2 mb-4">
        <input
          value={region}
          onChange={(e) => setRegion(e.target.value)}
          placeholder="지역 검색 (예: 서울, 부산, 제주)"
          className="flex-1 px-3 py-2 border rounded-lg"
        />
        <button type="submit" className="px-4 py-2 bg-green-700 text-white rounded-lg hover:bg-green-800">검색</button>
      </form>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      {weather?.region && (
        <div className="flex items-center gap-2 text-lg font-semibold text-gray-800 mb-4">
          <MapPin className="w-5 h-5 text-green-700" />
          {weather.region} 날씨
        </div>
      )}

      {weather?.current && (
        <div className="mb-6 p-4 bg-blue-50 rounded-xl border border-blue-100 flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-600">현재 날씨</p>
            <p className="text-2xl font-bold text-gray-900">{weather.current.condition} {weather.current.temp}°C</p>
            <p className="text-sm text-gray-600">풍속 {weather.current.wind_speed}km/h · 습도 {weather.current.humidity}%</p>
          </div>
          <div className="text-right">
            {weatherIcon(weather.current.condition)}
          </div>
        </div>
      )}

      {weather?.data?.length === 0 ? (
        <p className="text-gray-500">날씨 정보가 없습니다.</p>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
          {weather?.data?.map((day, idx) => (
            <div key={idx} className={`bg-white border rounded-xl p-4 text-center ${idx === 0 ? 'border-green-400 ring-1 ring-green-100' : 'border-gray-200'}`}>
              <p className="text-sm text-gray-500">{day.date}</p>
              <p className="font-semibold">{day.day}</p>
              <div className="flex justify-center my-2">{weatherIcon(day.condition)}</div>
              <p className="text-sm text-gray-700">{day.condition}</p>
              <p className="text-sm font-medium">{day.temp_max}° / {day.temp_min}°</p>
              <p className="text-xs text-gray-500 mt-1">습도 {day.humidity}% · 강수 {day.rain_prob}%</p>
            </div>
          ))}
        </div>
      )}

      <div className="mt-8 p-4 bg-green-50 rounded-xl border border-green-100">
        <div className="flex items-center gap-2 mb-3">
          <Sprout className="w-5 h-5 text-green-700" />
          <p className="font-semibold text-green-800">작물별 날씨 최적화 조언</p>
        </div>
        <select
          value={selectedCrop}
          onChange={(e) => setSelectedCrop(e.target.value)}
          className="w-full px-3 py-2 border rounded-lg mb-3"
        >
          <option value="">{myCropOption(crops)}</option>
          {crops.length > 0 && <optgroup label="내 작물">
            {crops.map((c) => <option key={c.id} value={c.name}>{c.name}</option>)}
          </optgroup>}
          <optgroup label="작물 가이드">
            {cropGuides.map((c) => <option key={c.name} value={c.name}>{c.name}</option>)}
          </optgroup>
        </select>

        {!selectedCrop && (
          <p className="text-sm text-gray-600">작물을 선택하면 오늘 날씨에 따른 재배 적합도와 맞춤 조언을 확인할 수 있습니다.</p>
        )}

        {selectedCrop && guide && (
          <div className="space-y-2">
            <p className="text-sm text-gray-600">
              적정 환경: 온도 {guide.optimalTemp} / 습도 {guide.humidity}
            </p>
            {suitability && (
              <span className={`inline-block px-3 py-1 rounded-full text-sm font-semibold ${suitabilityClass(suitability)}`}>
                {suitabilityLabel(suitability)}
              </span>
            )}
            {advice && <p className="text-sm text-gray-800">{advice}</p>}
          </div>
        )}
      </div>

      <p className="mt-6 text-xs text-gray-400 text-center">기상 데이터: Open-Meteo (실시간 기상 예보)</p>
    </PageCard>
  )
}

function myCropOption(crops: Crop[]) {
  if (crops.length === 0) return '작물 선택'
  return `내 작물 선택 (${crops.length}개)`
}

function suitabilityClass(s: 'good' | 'warning' | 'bad') {
  if (s === 'good') return 'bg-green-100 text-green-800'
  if (s === 'warning') return 'bg-yellow-100 text-yellow-800'
  return 'bg-red-100 text-red-800'
}

function suitabilityLabel(s: 'good' | 'warning' | 'bad') {
  if (s === 'good') return '최적'
  if (s === 'warning') return '주의'
  return '부적합'
}

function weatherIcon(condition: string) {
  const c = condition || ''
  if (c.includes('비') || c.includes('소나기')) return <CloudRain className="w-8 h-8 text-blue-500" />
  if (c.includes('흐림') || c.includes('구름') || c.includes('안개')) return <Cloud className="w-8 h-8 text-gray-500" />
  if (c.includes('맑음') || c.includes('햇')) return <Sun className="w-8 h-8 text-yellow-500" />
  if (c.includes('눈')) return <Cloud className="w-8 h-8 text-blue-200" />
  if (c.includes('바람')) return <Wind className="w-8 h-8 text-teal-500" />
  return <CloudSun className="w-8 h-8 text-yellow-500" />
}
