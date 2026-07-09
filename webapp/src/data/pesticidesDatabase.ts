export interface PesticideEntry {
  name: string
  type: string
  category?: string
  ingredient?: string
  target: string
  crops?: string
  safetyPeriod?: string
  dilution?: string
  safety_period?: string
}

export const pesticidesDatabase: PesticideEntry[] = [
  // 살충제
  { name: '스피노사드', type: '살충제', category: '살충제', ingredient: 'Spinosad', target: '배추좀나방, 미국선녀벌레, 진딧물', crops: '배추, 고추, 수박, 토마토', safetyPeriod: '1일', dilution: '1,000배' },
  { name: '아바멕틴', type: '살충제', category: '살충제', ingredient: 'Abamectin', target: '진딧물, 응애, 파릇파릇나방', crops: '고추, 오이, 딸기', safetyPeriod: '3일', dilution: '2,000배' },
  { name: '이미다클로프리드', type: '살충제', category: '살충제', ingredient: 'Imidacloprid', target: '진딧물, 총채벌레, 벼멸구', crops: '벼, 과수, 채소', safetyPeriod: '7일', dilution: '2,500배' },
  { name: '피프로닐', type: '살충제', category: '살충제', ingredient: 'Fipronil', target: '메뚜기, 노린재, 흰가루이', crops: '벼, 채소', safetyPeriod: '7일', dilution: '2,000배' },
  { name: '시안트라닐립롤', type: '살충제', category: '살충제', ingredient: 'Cyantraniliprole', target: '배추좀나방, 토마토화영나방', crops: '배추, 고추, 토마토', safetyPeriod: '1일', dilution: '1,500배' },
  { name: '클로르피리포스', type: '살충제', category: '살충제', ingredient: 'Chlorpyrifos', target: '콩나물파리, 뿌리파리, 지렁이', crops: '콩, 고구마, 채소', safetyPeriod: '10일', dilution: '1,000배' },
  { name: '디아치논', type: '살충제', category: '살충제', ingredient: 'Diazinon', target: '진딧물, 노린재, 총채벌레', crops: '사과, 배추, 고추', safetyPeriod: '15일', dilution: '1,000배' },
  { name: '메타플루미존', type: '살충제', category: '살충제', ingredient: 'Metaflumizone', target: '배추좀나방, 돌발해충', crops: '배추, 고추', safetyPeriod: '3일', dilution: '1,500배' },
  { name: '프로마이트', type: '살충제(아카리)', category: '살충제', ingredient: 'Fenpyroximate', target: '응애, 진드기', crops: '사과, 배, 감귤', safetyPeriod: '7일', dilution: '2,000배' },
  { name: '플루벤다이아마이드', type: '살충제', category: '살충제', ingredient: 'Flubendiamide', target: '배추좀나방, 배나무좀나방', crops: '배추, 사과', safetyPeriod: '1일', dilution: '2,000배' },

  // 살균제
  { name: '만코제브', type: '살균제', category: '살균제', ingredient: 'Mancozeb', target: '노균병, 흰가루병, 탄저병', crops: '오이, 고추, 사과, 포도', safetyPeriod: '3일', dilution: '800배' },
  { name: '플루티카솔', type: '살균제', category: '살균제', ingredient: 'Flutianil', target: '흰가루병', crops: '오이, 수박, 호박', safetyPeriod: '1일', dilution: '3,000배' },
  { name: '디페노코나졸', type: '살균제', category: '살균제', ingredient: 'Difenoconazole', target: '노균병, 흰가루병, 녹병', crops: '사과, 배추, 고추', safetyPeriod: '3일', dilution: '3,000배' },
  { name: '아조시스트로빈', type: '살균제', category: '살균제', ingredient: 'Azoxystrobin', target: '탄저병, 노균병, 무름병', crops: '고추, 오이, 사과', safetyPeriod: '3일', dilution: '1,000배' },
  { name: '티오판산메틸', type: '살균제', category: '살균제', ingredient: 'Thiophanate-methyl', target: '탄저병, 흰가루병, 균핵병', crops: '사과, 딸기, 고추', safetyPeriod: '3일', dilution: '1,000배' },
  { name: '캅탄', type: '살균제', category: '살균제', ingredient: 'Captan', target: '탄저병, 잿빛곰팡이병', crops: '사과, 배, 포도', safetyPeriod: '15일', dilution: '800배' },
  { name: '보르드액', type: '살균제', category: '살균제', ingredient: 'Bordeaux mixture', target: '세균성검은별무늬병, 세균구멍병', crops: '사과, 배, 복숭아', safetyPeriod: '-', dilution: '2~5%액' },
  { name: '구리수화제', type: '살균제', category: '살균제', ingredient: 'Copper hydroxide', target: '세균성무름병, 점무늬병', crops: '오이, 고추, 토마토', safetyPeriod: '1일', dilution: '600배' },
  { name: '유황', type: '살균제(겸용)', category: '살균제', ingredient: 'Sulfur', target: '흰가루병, 진드기', crops: '오이, 참외, 복숭아', safetyPeriod: '3일', dilution: '300배' },
  { name: '트리플록시스트로빈', type: '살균제', category: '살균제', ingredient: 'Trifloxystrobin', target: '흰가루병, 탄저병', crops: '오이, 사과, 포도', safetyPeriod: '3일', dilution: '1,500배' },

  // 제초제
  { name: '글루포시네이트 암모늄', type: '제초제', category: '제초제', ingredient: 'Glufosinate-ammonium', target: '잡초(묘포기)', crops: '논, 밭, 과수원', safetyPeriod: '-', dilution: '희석 살포' },
  { name: '글리포세이트 이소프로필아민', type: '제초제', category: '제초제', ingredient: 'Glyphosate IPA', target: '잡초(전체)', crops: '논, 밭, 과수원', safetyPeriod: '-', dilution: '100~200배' },
  { name: '파라콰트', type: '제초제', category: '제초제', ingredient: 'Paraquat', target: '잡초(촉매법)', crops: '논, 밭', safetyPeriod: '-', dilution: '200배' },
  { name: '디쿠아트', type: '제초제', category: '제초제', ingredient: 'Diquat', target: '수서식물, 논 잡초', crops: '논', safetyPeriod: '-', dilution: '200배' },
  { name: '옥시플루오르펜', type: '제초제', category: '제초제', ingredient: 'Oxyfluorfen', target: '돋보기나물, 깨풀 등', crops: '고추, 마늘, 양파', safetyPeriod: '-', dilution: '500배' },
  { name: '펜디메탈린', type: '제초제', category: '제초제', ingredient: 'Pendimethalin', target: '한해살이 잡초', crops: '마늘, 양파, 콩', safetyPeriod: '-', dilution: '토양처리' },
  { name: '이미다졸리논계 제초제', type: '제초제', category: '제초제', ingredient: 'Imazapyr', target: '목본류, 잡초', crops: '제초 특수', safetyPeriod: '-', dilution: '희석 살포' },

  // 비료/영양제
  { name: '유박 비료', type: '유기질비료', category: '비료', ingredient: '유기물', target: '토양 개량 및 작물 생육 촉진', crops: '모든 작물', safetyPeriod: '-', dilution: '토양 시비' },
  { name: '복합비료 20-20-20', type: '화학비료', category: '비료', ingredient: 'N-P-K', target: '생육 초기 전반', crops: '모든 작물', safetyPeriod: '-', dilution: '1,000배' },
  { name: '황산칼륨', type: '화학비료', category: '비료', ingredient: 'K2SO4', target: '과실비대, 착색', crops: '과수, 고추, 토마토', safetyPeriod: '-', dilution: '500배' },
  { name: '질산칼슘', type: '화학비료', category: '비료', ingredient: 'Ca(NO3)2', target: '칼슘 결핍 예방', crops: '토마토, 고추, 배추', safetyPeriod: '-', dilution: '1,000배' },
  { name: '엽록소(히마트)', type: '영양제', category: '비료', ingredient: '엽록소유도체', target: '엽록소 증진, 광합성 촉진', crops: '녹용 작물', safetyPeriod: '-', dilution: '500배' },
]
