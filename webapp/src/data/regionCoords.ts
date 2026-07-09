export interface GeoCoord {
  name: string
  lat: number
  lon: number
}

export const regionCoords: GeoCoord[] = [
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

export function findRegionCoord(query: string): GeoCoord | undefined {
  const q = query.trim()
  if (!q) return regionCoords[0]
  const matched = regionCoords.find(
    (r) => r.name === q || r.name.includes(q) || q.includes(r.name)
  )
  if (matched) return matched
  if (q.includes('전국')) return regionCoords[0]
  return undefined
}

export function wmoCodeToKorean(code: number): string {
  if (code === 0) return '맑음'
  if ([1, 2, 3].includes(code)) return '구름 조금'
  if ([45, 48].includes(code)) return '안개'
  if ([51, 53, 55, 56, 57].includes(code)) return '이슬비'
  if ([61, 63, 65, 66, 67, 80, 81, 82].includes(code)) return '비'
  if ([71, 73, 75, 77, 85, 86].includes(code)) return '눈'
  if ([95, 96, 99].includes(code)) return '뇌우'
  return '흐림'
}
