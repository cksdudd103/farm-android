const express = require('express')
const path = require('path')
const fetch = require('node-fetch')

const app = express()
const PORT = process.env.PORT || 3000
const BACKEND_URL = (process.env.BACKEND_URL || 'https://farm-webapp-rezy.onrender.com/api').replace(/\/$/, '')

console.log('BACKEND_URL:', BACKEND_URL)

function getDayName(dateStr) {
  const days = ['일', '월', '화', '수', '목', '금', '토']
  return days[new Date(dateStr).getDay()]
}

function wmoToKorean(code) {
  if (code === 0) return '맑음'
  if ([1, 2, 3].includes(code)) return '구름 조금'
  if ([45, 48].includes(code)) return '안개'
  if ([51, 53, 55, 56, 57].includes(code)) return '이슬비'
  if ([61, 63, 65, 66, 67, 80, 81, 82].includes(code)) return '비'
  if ([71, 73, 75, 77, 85, 86].includes(code)) return '눈'
  if ([95, 96, 99].includes(code)) return '뇌우'
  return '흐림'
}

const regionCoords = [
  { name: '서울', lat: 37.5665, lon: 126.9780 },
  { name: '부산', lat: 35.1796, lon: 129.0756 },
  { name: '대구', lat: 35.8714, lon: 128.6014 },
  { name: '인천', lat: 37.4563, lon: 126.7052 },
  { name: '광주', lat: 35.1595, lon: 126.8526 },
  { name: '대전', lat: 36.3504, lon: 127.3845 },
  { name: '울산', lat: 35.5384, lon: 129.3114 },
  { name: '세종', lat: 36.48, lon: 127.289 },
  { name: '경기', lat: 37.4138, lon: 127.5183 },
  { name: '강원', lat: 37.8228, lon: 128.1555 },
  { name: '충북', lat: 36.6357, lon: 127.4914 },
  { name: '충남', lat: 36.5184, lon: 126.8 },
  { name: '전북', lat: 35.8242, lon: 127.148 },
  { name: '전남', lat: 34.816, lon: 126.4629 },
  { name: '경북', lat: 36.019, lon: 129.3435 },
  { name: '경남', lat: 35.4606, lon: 128.2132 },
  { name: '제주', lat: 33.4996, lon: 126.5312 },
]

function findRegion(query) {
  const q = (query || '').trim()
  if (!q) return regionCoords[0]
  const matched = regionCoords.find((r) => r.name === q || r.name.includes(q) || q.includes(r.name))
  if (matched) return matched
  return regionCoords[0]
}

app.get('/api/weather', async (req, res) => {
  try {
    const region = findRegion(req.query.region)
    const url = `https://api.open-meteo.com/v1/forecast?latitude=${region.lat}&longitude=${region.lon}&current_weather=true&daily=weathercode,temperature_2m_max,temperature_2m_min,precipitation_probability_max,relative_humidity_2m_mean&timezone=Asia%2FSeoul&forecast_days=6`
    const response = await fetch(url)
    const json = await response.json()
    if (!json.daily) throw new Error('Invalid weather response')

    const daily = []
    for (let i = 0; i < json.daily.time.length; i++) {
      daily.push({
        date: json.daily.time[i],
        day: getDayName(json.daily.time[i]),
        condition: wmoToKorean(json.daily.weathercode[i]),
        temp_max: Math.round(json.daily.temperature_2m_max[i]),
        temp_min: Math.round(json.daily.temperature_2m_min[i]),
        humidity: Math.round(json.daily.relative_humidity_2m_mean[i]),
        rain_prob: json.daily.precipitation_probability_max[i],
      })
    }

    res.json({
      ok: true,
      region: region.name,
      current: {
        temp: Math.round(json.current_weather.temperature),
        condition: wmoToKorean(json.current_weather.weathercode),
        wind_speed: json.current_weather.windspeed,
        humidity: daily[0]?.humidity ?? 0,
      },
      data: daily,
    })
  } catch (err) {
    console.error('[WEATHER ERROR]', err.message)
    res.status(502).json({ ok: false, msg: '날씨 정보를 불러오지 못했습니다.' })
  }
})

const externalLinks = [
  { name: '농촌진흥청', url: 'https://www.rda.go.kr', category: '정부기관', description: '농업 기술, 병해충 정보, 영농 자료' },
  { name: '농촌진흥청 공지사항', url: 'https://www.rda.go.kr/board/board.do?boardId=farmprmntinfo&prgId=day_farmprmntinfoEntry&currIndex=1&searchKey=&searchVal=&dataNo=0000000000', category: '공지사항', description: '농촌진흥청 최신 공지 및 소식' },
  { name: '농사로', url: 'https://www.nongsaro.go.kr', category: '정보포털', description: '농업 기술, 작물 정보, 병해충 진단' },
  { name: '농림축산식품부', url: 'https://www.mafra.go.kr', category: '정부기관', description: '농식품 정책, 지원 사업, 병해충 발생 동향' },
  { name: '농림축산식품부 본부공지', url: 'https://www.mafra.go.kr/home/5004/subview.do', category: '공지사항', description: '농식품부 공지사항' },
  { name: '기상청 날씨누리', url: 'https://www.weather.go.kr', category: '기상정보', description: '기상청 공식 날씨 예보 및 특보' },
  { name: '농업관측', url: 'https://www.agweather.go.kr', category: '기상정보', description: '농업 기상 관측 및 예보' },
  { name: '농산물유통정보(KAMIS)', url: 'https://www.kamis.or.kr', category: '시세정보', description: '농산물 도매가격 및 소매가격 정보' },
  { name: '농촌일자리진흥청', url: 'https://www.rda.go.kr/youngfarmer', category: '지원사업', description: '청년 농업인 및 귀농 지원' },
  { name: '귀농귀촌종합센터', url: 'https://www.returnfarm.com', category: '지원사업', description: '귀농·귀촌 상담 및 교육' },
]

const announcements = [
  { title: '농촌진흥청 홈페이지 바로가기', url: 'https://www.rda.go.kr', source: '농촌진흥청', date: '', summary: '공지사항은 외부 사이트에서 직접 확인해주세요.' },
  { title: '농사로 병해충 정보', url: 'https://www.nongsaro.go.kr/portal/ps/psb/psbb/farmNocticeList.ps', source: '농사로', date: '', summary: '병해충 예보 및 방제 정보' },
  { title: '농림축산식품부 정책뉴스', url: 'https://www.mafra.go.kr/home/5013/subview.do', source: '농식품부', date: '', summary: '정책 소식 및 병해충 발생 현황' },
]

app.get('/api/external-links', (req, res) => {
  res.json({ ok: true, data: externalLinks })
})

app.get('/api/announcements', (req, res) => {
  const source = req.query.source
  const data = source ? announcements.filter((a) => a.source === source) : announcements
  res.json({ ok: true, data })
})

app.use('/api', async (req, res) => {
  if (req.url === '/health' || req.url === '/health/') {
    return res.json({ ok: true, backend: BACKEND_URL, timestamp: new Date().toISOString() })
  }

  const targetUrl = BACKEND_URL + req.url
  console.log('[PROXY]', req.method, req.url, '->', targetUrl)

  try {
    const chunks = []
    for await (const chunk of req) {
      chunks.push(chunk)
    }
    const body = Buffer.concat(chunks)

    const headers = {}
    Object.entries(req.headers).forEach(([key, value]) => {
      if (key !== 'host' && key !== 'content-length') {
        headers[key] = value
      }
    })
    headers.host = new URL(BACKEND_URL).hostname
    if (body.length > 0) {
      headers['content-length'] = String(body.length)
    }

    const response = await fetch(targetUrl, {
      method: req.method,
      headers,
      body: body.length > 0 ? body : undefined,
      redirect: 'manual',
      credentials: 'include',
    })

    res.status(response.status)
    response.headers.forEach((value, key) => {
      if (key === 'set-cookie') {
        res.setHeader('set-cookie', response.headers.raw()['set-cookie'])
      } else if (key !== 'content-encoding' && key !== 'transfer-encoding') {
        res.setHeader(key, value)
      }
    })
    response.body.pipe(res)
  } catch (err) {
    console.error('[PROXY ERROR]', err.message)
    res.status(502).json({ ok: false, error: 'Proxy error: ' + err.message })
  }
})

app.use(express.json({ limit: '10mb' }))
app.use(express.urlencoded({ extended: true, limit: '10mb' }))

app.use(express.static(path.join(__dirname, 'dist')))

app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist', 'index.html'))
})

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`)
})
