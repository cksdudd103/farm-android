export interface CropGuide {
  name: string
  category: string
  sowingSeason: string
  harvestSeason: string
  temperature: string
  humidity: string
  soil: string
  watering: string
  fertilizing: string
  commonPests: string[]
  commonDiseases: string[]
  recommendedPesticides: PesticideGuide[]
  tips: string[]
}

export interface PesticideGuide {
  name: string
  type: string
  target: string
  dilution: string
  safetyPeriod: string
}

export const cropGuides: CropGuide[] = [
  {
    name: '고추',
    category: '채소',
    sowingSeason: '2~3월',
    harvestSeason: '6~10월',
    temperature: '20~28°C',
    humidity: '60~70%',
    soil: '배수가 잘 되는 비옥한 사양토',
    watering: '겉흙이 마를 때 충분히, 과습 주의',
    fertilizing: '유박 비료 기본, 과실비료 2~3회 추가',
    commonPests: ['진딧물', '응애', '고추가루이', '흰가루이'],
    commonDiseases: ['흰가루병', '탄저병', '역병', '바이러스병'],
    recommendedPesticides: [
      { name: '스미치온', type: '살충제', target: '진딧물, 응애', dilution: '1,000배', safetyPeriod: '수확 7일 전' },
      { name: '다이센', type: '살균제', target: '흰가루병, 탄저병', dilution: '800배', safetyPeriod: '수확 3일 전' },
    ],
    tips: ['햇빛이 잘 드는 곳에서 재배', '과습 방지를 위해 배수 관리', '정기적으로 잡초 제거'],
  },
  {
    name: '배추',
    category: '채소',
    sowingSeason: '3~4월, 8~9월',
    harvestSeason: '5~6월, 10~11월',
    temperature: '15~20°C',
    humidity: '70~80%',
    soil: '유기질이 풍부하고 보습력 좋은 흙',
    watering: '규칙적으로, 결구기에 충분히',
    fertilizing: '기본 퇴비 + 질소 비료 결구기 직전 추가',
    commonPests: ['배추좀나방', '배추흰나비', '진딧물', '벼룩잎벌레'],
    commonDiseases: ['무름병', '검은썩음병', '노균병'],
    recommendedPesticides: [
      { name: '미팔', type: '살충제', target: '배추좀나방, 배추흰나비', dilution: '1,000배', safetyPeriod: '수확 7일 전' },
      { name: '뉴펜타조', type: '살균제', target: '무름병', dilution: '1,000배', safetyPeriod: '수확 5일 전' },
    ],
    tips: ['연작 피하기', '충분한 햇빛 필요', '결구기 물 관리 중요'],
  },
  {
    name: '오이',
    category: '채소',
    sowingSeason: '3~4월, 7~8월',
    harvestSeason: '5~6월, 9~10월',
    temperature: '22~28°C',
    humidity: '70~80%',
    soil: '통기성 좋고 수분 보유력 있는 흙',
    watering: '아침에 충분히, 잎에 물 고이지 않게',
    fertilizing: '유박 + 복합비료, 개화기 질소 감소',
    commonPests: ['응애', '진딧물', '온실가루이', '파밤나방'],
    commonDiseases: ['흰가루병', '노균병', '역병', '탄저병'],
    recommendedPesticides: [
      { name: '다이센', type: '살균제', target: '흰가루병, 노균병', dilution: '800배', safetyPeriod: '수확 3일 전' },
      { name: '케미파이트', type: '살충제', target: '응애, 진딧물', dilution: '1,500배', safetyPeriod: '수확 5일 전' },
    ],
    tips: ['덩굴 받침대 설치', '잎물림 방지', '수확 시 조심스럽게'],
  },
  {
    name: '토마토',
    category: '채소',
    sowingSeason: '2~3월',
    harvestSeason: '6~9월',
    temperature: '20~26°C',
    humidity: '60~70%',
    soil: 'pH 6.0~6.8의 배수良好的한 흙',
    watering: '줄기 부근에 규칙적으로, 과습 금지',
    fertilizing: '과인산비료 중심, 착색기 칼륨 추가',
    commonPests: ['진딧물', '응애', '방패벌레', '토마토화염병해충'],
    commonDiseases: ['흰가루병', '잎곰팡이병', '바이러스병', '흑색과침병'],
    recommendedPesticides: [
      { name: '스미치온', type: '살충제', target: '진딧물, 응애', dilution: '1,000배', safetyPeriod: '수확 7일 전' },
      { name: '프루나', type: '살균제', target: '흰가루병, 잎곰팡이병', dilution: '3,000배', safetyPeriod: '수확 15일 전' },
    ],
    tips: ['덩굴 제거 및 받침대 설치', '하우스 재배 시 환기', '규칙적인 방제'],
  },
  {
    name: '상추',
    category: '채소',
    sowingSeason: '3~4월, 9~10월',
    harvestSeason: '5~6월, 11~12월',
    temperature: '15~20°C',
    humidity: '70~80%',
    soil: '보습력 좋은 부식질 흙',
    watering: '자주, 물부족 시 쓴 맛 발생',
    fertilizing: '질소 비료를 적당히, 과다 시 질병 발생',
    commonPests: ['진딧물', '응애', '닭다귀벌레', '배추흰나비'],
    commonDiseases: ['무름병', '노균병', '흰가루병'],
    recommendedPesticides: [
      { name: '미팔', type: '살충제', target: '배추흰나비, 진딧물', dilution: '1,000배', safetyPeriod: '수확 7일 전' },
      { name: '다이센', type: '살균제', target: '흰가루병', dilution: '800배', safetyPeriod: '수확 3일 전' },
    ],
    tips: ['햇빛이 적당한 곳', '수확 전 안전기간 준수', '연작 피하기'],
  },
  {
    name: '무',
    category: '채소',
    sowingSeason: '3~4월, 8~9월',
    harvestSeason: '5~6월, 10~11월',
    temperature: '15~18°C',
    humidity: '60~70%',
    soil: '깊고 부드러운 사양토',
    watering: '균일하게, 뿌리 형성기 충분히',
    fertilizing: '유박 + 칼륨 비료, 결구기 직전 추가',
    commonPests: ['배추좀나방', '뿌리응애', '진딧물'],
    commonDiseases: ['무름병', '검은썩음병', '바이러스병'],
    recommendedPesticides: [
      { name: '미팔', type: '살충제', target: '배추좀나방', dilution: '1,000배', safetyPeriod: '수확 7일 전' },
      { name: '뉴펜타조', type: '살균제', target: '무름병', dilution: '1,000배', safetyPeriod: '수확 5일 전' },
    ],
    tips: ['뿌리가 뻗을 수 있게 경운', '과밀 식재 피하기', '수확 시 뿌리 손상 주의'],
  },
  {
    name: '사과',
    category: '과수',
    sowingSeason: '봄(3~4월)',
    harvestSeason: '9~10월',
    temperature: '12~25°C',
    humidity: '60~70%',
    soil: '배수良好, pH 6.0~6.5',
    watering: '건조기 충분히, 과습 피하기',
    fertilizing: '춘광비료, 추광비료 규칙적으로',
    commonPests: ['사과좀나방', '응애', '진딧물', '화염병'],
    commonDiseases: ['흰가루병', '검은별무늬병', '부패병'],
    recommendedPesticides: [
      { name: '프루나', type: '살균제', target: '흰가루병, 검은별무늬병', dilution: '3,000배', safetyPeriod: '수확 15일 전' },
      { name: '스미치온', type: '살충제', target: '사과좀나방, 진딧물', dilution: '1,000배', safetyPeriod: '수확 21일 전' },
    ],
    tips: ['적과 및 순집기 중요', '겨울 전剪定', '정기 방제 철저'],
  },
  {
    name: '배',
    category: '과수',
    sowingSeason: '봄(3~4월)',
    harvestSeason: '9~10월',
    temperature: '12~25°C',
    humidity: '60~70%',
    soil: '깊고 비옥한 사양토',
    watering: '생육기 균일하게, 수확前 2주 감수',
    fertilizing: '유기질 비료 기본, NPK 균형',
    commonPests: ['배나방', '응애', '진딧물'],
    commonDiseases: ['흰가루병', '갈색빗자루병', '부패병'],
    recommendedPesticides: [
      { name: '프루나', type: '살균제', target: '갈색빗자루병, 흰가루병', dilution: '3,000배', safetyPeriod: '수확 15일 전' },
      { name: '스미치온', type: '살충제', target: '배나방, 진딧물', dilution: '1,000배', safetyPeriod: '수확 21일 전' },
    ],
    tips: ['적과하여 품질 향상', '바람 통과良好的하게', '수확 후 저장 온도 관리'],
  },
  {
    name: '복숭아',
    category: '과수',
    sowingSeason: '봄(3~4월)',
    harvestSeason: '6~8월',
    temperature: '15~28°C',
    humidity: '60~70%',
    soil: '배수良好한 사양토',
    watering: '착과기, 과비대기 충분히',
    fertilizing: '유박 + 칼륨 중심 비료',
    commonPests: ['복숭아심식나방', '응애', '진딧물'],
    commonDiseases: ['세균성구멍병', '흰가루병', '탄저병'],
    recommendedPesticides: [
      { name: '프루나', type: '살균제', target: '세균성구멍병, 흰가루병', dilution: '3,000배', safetyPeriod: '수확 15일 전' },
      { name: '스미치온', type: '살충제', target: '복숭아심식나방', dilution: '1,000배', safetyPeriod: '수확 21일 전' },
    ],
    tips: ['가지치기로 햇빛 유입', '과다 착과 방지', '수확 후 냉장 보관'],
  },
  {
    name: '딸기',
    category: '과채',
    sowingSeason: '8~9월 정식',
    harvestSeason: '12~5월',
    temperature: '15~22°C',
    humidity: '70~80%',
    soil: '산성토 양호, 배수良好的',
    watering: '고랑에 충분히, 잎에 묻지 않게',
    fertilizing: '시비 후 정기 추비, 과다 질소 주의',
    commonPests: ['응애', '진딧물', '딸기뿌리응애', '작은뿌리파리'],
    commonDiseases: ['흰가루병', '잿빛곰팡이병', '역병'],
    recommendedPesticides: [
      { name: '다이센', type: '살균제', target: '흰가루병, 잿빛곰팡이병', dilution: '800배', safetyPeriod: '수확 3일 전' },
      { name: '케미파이트', type: '살충제', target: '응애, 진딧물', dilution: '1,500배', safetyPeriod: '수확 5일 전' },
    ],
    tips: ['하우스 재배 시 환기', '고온다습 피하기', '정기 순 제거'],
  },
  {
    name: '감자',
    category: '채소',
    sowingSeason: '3~4월',
    harvestSeason: '6~7월',
    temperature: '15~20°C',
    humidity: '60~70%',
    soil: '배수良好, 깊은 사양토',
    watering: '개화기~괴경비대기 충분히',
    fertilizing: '인산, 칼륨 중심, 질소는 적게',
    commonPests: ['감자잎말이나방', '진딧물', '응애'],
    commonDiseases: ['감자역병', '흑색썩음병', '바이러스병'],
    recommendedPesticides: [
      { name: '뉴펜타조', type: '살균제', target: '감자역병', dilution: '1,000배', safetyPeriod: '수확 7일 전' },
      { name: '미팔', type: '살충제', target: '감자잎말이나방', dilution: '1,000배', safetyPeriod: '수확 7일 전' },
    ],
    tips: ['정식 깊이 5~8cm', '중간에 흙 올려주기', '수확 후 어둡고 서늘한 곳 저장'],
  },
  {
    name: '고구마',
    category: '채소',
    sowingSeason: '5월',
    harvestSeason: '9~10월',
    temperature: '20~28°C',
    humidity: '60~70%',
    soil: '배수良好한 사양토, 모래 흙',
    watering: '건조 시 충분히, 수확前 2주 감수',
    fertilizing: '유박 기본, 칼륨 비료 중심',
    commonPests: ['고구마바이러스병해충', '진딧물', '응애'],
    commonDiseases: ['검은무늬병', '줄기썩음병'],
    recommendedPesticides: [
      { name: '다이센', type: '살균제', target: '검은무늬병', dilution: '800배', safetyPeriod: '수확 7일 전' },
      { name: '스미치온', type: '살충제', target: '진딧물, 응애', dilution: '1,000배', safetyPeriod: '수확 7일 전' },
    ],
    tips: ['심을 땅 고랑 만들기', '잡초 제거 철저', '수확 후 햇볕에 말리기'],
  },
]

export function getCropGuide(name: string): CropGuide | undefined {
  return cropGuides.find((c) => c.name === name || name.includes(c.name))
}

export function searchCropGuides(q: string): CropGuide[] {
  if (!q.trim()) return cropGuides
  const lower = q.toLowerCase()
  return cropGuides.filter(
    (c) =>
      c.name.toLowerCase().includes(lower) ||
      c.category.toLowerCase().includes(lower) ||
      c.commonPests.some((p) => p.toLowerCase().includes(lower)) ||
      c.commonDiseases.some((d) => d.toLowerCase().includes(lower))
  )
}
