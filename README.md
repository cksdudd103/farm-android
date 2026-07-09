# 스마트영농 (Smart Farming) - Android

Flask 기반 농업 관리 백엔드([farm-webapp](../farm-webapp))와 연동되는 Jetpack Compose 안드로이드 앱입니다.

## 주요 기능

- 로그인 (실제 백엔드 계정)
- 대시보드 (통계 카드, 차트)
- 작물 관리, 영농 일지, 작업 일정, 재고 관리
- AI 병해충 진단 (카메라 촬영 업로드)
- 농산물 시세, 날씨 예보, 농약 정보, 정부 지원사업, 농업진흥청 새소식
- 커뮤니티, 출하 관리, 농작업 안전, 요금제, 회원 등급/프로필
- 관리자 전용: 사용자 관리, 요금제 관리, 등급 관리
- 농업 관련 사이트 링크

모바일에서는 하단 내비게이션(대시보드/작물/일지/더보기), 태블릿·데스크톱 폭에서는 좌측 드로어(서랍) 내비게이션을 사용합니다.

## 기술 스택

- Kotlin + Jetpack Compose (Material 3)
- Retrofit2 + OkHttp3 (REST API 통신, 쿠키 기반 세션 유지)
- Coil (이미지 로딩)
- Room (작물/일지/작업 오프라인 캐시)
- Jetpack DataStore (백엔드 주소·세션 정보 저장)
- MPAndroidChart (대시보드 통계 차트)
- Accompanist Permissions (카메라 권한 처리)

## 프로젝트 구조

```
farm-android/
├── build.gradle.kts            # 프로젝트 레벨 빌드 설정
├── settings.gradle.kts         # 모듈/저장소 설정 (JitPack 포함)
├── gradle.properties
├── gradlew / gradlew.bat       # Gradle 래퍼 실행 스크립트
├── gradle/wrapper/             # Gradle 래퍼 설정 및 jar
└── app/
    ├── build.gradle.kts        # 앱 모듈 빌드 설정 (BASE_URL 등 BuildConfig)
    └── src/main/
        ├── AndroidManifest.xml # 인터넷/카메라 권한, FileProvider 설정
        ├── java/com/smartfarm/app/
        │   ├── MainActivity.kt
        │   ├── SmartFarmApp.kt        # Application, AppContainer 보관
        │   ├── data/                  # AppContainer(DI), 모델, Retrofit, Room, 저장소
        │   ├── ui/
        │   │   ├── AppViewModel.kt    # 세션 상태 공용 ViewModel
        │   │   ├── navigation/        # Screen 정의, 하단내비/드로어 메뉴 구성
        │   │   ├── theme/             # Green Nature 테마
        │   │   ├── components/        # 공용 Compose 컴포넌트
        │   │   └── screens/           # 화면별 Screen + ViewModel
        │   └── util/                  # Constants, ApiResult 등
        └── res/                       # strings, colors, themes, drawable, xml(config)
```

## 백엔드 연동 (BASE_URL 설정)

백엔드 Flask 서버 주소는 다음 두 곳에서 설정할 수 있습니다.

기본값은 비어 있으며(`BASE_URL = ""`), 앱을 처음 실행하면 서버 주소가 설정될 때까지 "설정" 화면이 강제로 표시됩니다.

1. **빌드 시점 기본값(선택)** – `app/build.gradle.kts`
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"\"")
   ```
   특정 서버 주소를 빌드에 고정하고 싶다면 이 값을 채운 뒤 다시 빌드하세요. (Windows에서 IP 확인: `ipconfig` 실행 후 IPv4 주소 확인)

2. **앱 실행 중 변경** – "설정" 화면에서 서버 주소를 언제든지 변경할 수 있습니다. 이 값은 DataStore에 저장되어 앱을 재시작해도 유지되며, 즉시 Retrofit 클라이언트가 재구성됩니다. APK를 재빌드하지 않고도 다른 Flask 서버로 바로 연결할 수 있습니다.

> Flask 백엔드는 Flask-Login 쿠키 기반 세션을 사용합니다. 앱은 OkHttp CookieJar로 세션 쿠키를 SharedPreferences에 직렬화하여 로그인 상태를 앱 재시작 후에도 유지합니다.

## 빌드 방법 (APK 만들기)

### 앱 기본 정보

| 항목 | 값 |
|---|---|
| 앱 이름 | 스마트영농 (Smart Farming) |
| 패키지명 | com.smartfarm.app |
| minSdk | 24 (Android 7.0 이상) |
| targetSdk | 34 |
| versionCode / versionName | 1 / 1.0.0 |

### 사전 준비

- Android Studio (Koala 이상 권장) — 설치 시 JDK와 Android SDK가 함께 설치됩니다.
- 또는 커맨드라인만 사용할 경우: JDK 17 이상 + Android SDK (`platforms;android-34` 또는 상위, `build-tools`), `ANDROID_HOME`/`sdk.dir` 설정
- 백엔드 서버(`farm-webapp`)가 실행 중이고, 안드로이드 기기/에뮬레이터에서 접근 가능한 네트워크 환경

### 방법 1: Android Studio에서 빌드 (권장, 가장 쉬움)

1. Android Studio 실행 → `Open` → `farm-android` 폴더 선택
2. Gradle Sync 완료 대기 (최초 실행 시 Gradle 8.9, AGP 8.5.2, Kotlin 2.0.21 등 다운로드 — 인터넷 필요)
3. (선택) `app/build.gradle.kts`의 `BASE_URL`을 백엔드 서버 IP로 수정 — 비워두면 앱 최초 실행 시 "설정" 화면에서 입력하게 됩니다.
4. 상단 메뉴 `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)` 클릭
5. 빌드 완료 후 우측 하단 알림의 `locate` 링크를 클릭하면 생성된 APK 폴더가 열립니다.
   - 경로: `app/build/outputs/apk/debug/app-debug.apk`
6. 실기기 테스트: USB로 폰을 연결하고 상단 실행 버튼(▶)을 클릭하면 바로 설치·실행됩니다.

### 방법 2: 커맨드라인(Gradle Wrapper)으로 빌드

```bash
# Windows (PowerShell/cmd)
gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

빌드가 성공하면 다음 경로에 설치 가능한 APK가 생성됩니다.

```
app/build/outputs/apk/debug/app-debug.apk
```

> `gradlew`/`gradlew.bat`가 정상 동작하려면 `gradle/wrapper/gradle-wrapper.jar` 파일과 `local.properties`(SDK 경로, `sdk.dir=...`)가 필요합니다. `local.properties`가 없다면 프로젝트 루트에 아래처럼 직접 만드세요.
>
> ```properties
> sdk.dir=C\:\\Users\\사용자이름\\AppData\\Local\\Android\\Sdk
> ```
> (Android Studio에서 한 번이라도 프로젝트를 열면 자동 생성됩니다.)

이 프로젝트는 실제로 Windows + Android Studio JBR(JDK 21) + Android SDK 환경에서 `gradlew.bat assembleDebug` 명령으로 **빌드 성공(BUILD SUCCESSFUL)** 이 확인되었으며, 약 22.7MB 크기의 `app-debug.apk`가 생성됩니다.

### 폰에 APK 설치하는 방법 (한국어 안내)

1. **APK 파일을 폰으로 전달**: USB 케이블로 연결해 파일 복사, 또는 카카오톡/이메일/구글 드라이브 등으로 `app-debug.apk` 파일을 폰에 전송합니다.
2. **출처를 알 수 없는 앱 설치 허용**:
   - Android 8.0 이상: 파일 관리자(또는 다운로드 앱)에서 APK 파일을 탭하면 "이 출처의 앱을 허용하시겠습니까?" 팝업이 뜹니다 → `설정` → 해당 앱(파일 관리자/브라우저 등) 토글 켜기 → 뒤로 가서 다시 APK 탭
   - 수동 설정 경로: `설정` → `앱` (또는 `애플리케이션`) → `특별 접근` → `알 수 없는 앱 설치` → APK를 열 앱(예: 내 파일, Chrome) 선택 → `이 출처 허용` 켜기
3. **APK 탭하여 설치**: 파일 관리자에서 `app-debug.apk` 파일을 찾아 탭 → `설치` 버튼 클릭 → 설치 완료 후 `열기`
4. **앱 최초 실행**: 서버 주소를 입력하는 "설정" 화면이 자동으로 표시됩니다. Flask 서버 주소(Base URL)를 입력 후 저장하면 로그인 화면으로 이동합니다.
5. **서버 주소를 나중에 변경하려면**: 더보기 메뉴 → 설정 화면에서 언제든지 다시 입력 후 저장

### 서명된 릴리스 APK 만들기 (배포용, 선택 사항)

디버그 APK는 자동 생성되는 디버그 키로 서명되어 개인적으로 설치·테스트하기에는 충분하지만, 여러 사람에게 배포하려면 릴리스 키로 서명하는 것을 권장합니다.

1. 릴리스 키스토어 생성 (최초 1회):
   ```bash
   keytool -genkeypair -v -keystore release-key.jks -alias smartfarm -keyalg RSA -keysize 2048 -validity 10000
   ```
2. 프로젝트 루트의 `gradle.properties`(또는 환경변수)에 아래 값을 추가 — **이 파일은 절대 공개 저장소에 커밋하지 마세요**:
   ```properties
   RELEASE_STORE_FILE=release-key.jks
   RELEASE_STORE_PASSWORD=본인이_설정한_비밀번호
   RELEASE_KEY_ALIAS=smartfarm
   RELEASE_KEY_PASSWORD=본인이_설정한_비밀번호
   ```
3. 릴리스 APK 빌드:
   ```bash
   gradlew.bat assembleRelease
   ```
4. 생성 경로: `app/build/outputs/apk/release/app-release.apk`

위 서명 정보를 설정하지 않으면 `assembleRelease`도 디버그 키로 자동 서명되어 빌드는 성공하지만, Play 스토어 배포에는 사용할 수 없고 개인 설치용으로만 사용해야 합니다.

### 에뮬레이터에서 로컬 백엔드 접속 시 주의사항

- Android 에뮬레이터에서 호스트 PC의 `localhost`에 접속하려면 `http://10.0.2.2:5000/` 을 BASE_URL로 사용하세요.
- 실기기에서 접속하려면 PC와 같은 Wi-Fi 네트워크에 연결한 뒤, PC의 사설 IP(`http://192.168.x.x:5000/`)를 사용하세요.
- 개발 단계에서 HTTP(비HTTPS) 통신을 허용하도록 `network_security_config.xml`에 `cleartextTrafficPermitted="true"`가 설정되어 있습니다.

### 백엔드 서버 실행 방법 (farm-webapp)

1. `farm-webapp` 폴더에서 Python 가상환경 활성화 후 필요한 패키지 설치 (`pip install -r requirements.txt`)
2. Flask 서버 실행 (예: `python app.py` 또는 `flask run --host=0.0.0.0 --port=5000`)
   - `--host=0.0.0.0` 옵션을 반드시 사용해야 같은 네트워크의 폰에서 접속할 수 있습니다.
3. PC의 사설 IP 확인: `ipconfig` 실행 후 `IPv4 주소` 확인 (예: 192.168.0.10)
4. 폰과 PC가 동일한 Wi-Fi에 연결되어 있는지 확인
5. 앱의 "설정" 화면에서 `http://<PC의 IP>:5000/` 형태로 서버 주소 입력 후 저장

## 권한

- `INTERNET` – API 통신
- `CAMERA` – AI 진단 사진 촬영
- `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE` – 갤러리 이미지 업로드 (API 레벨에 따라 자동 분기)

## 오프라인 캐시

Room 데이터베이스를 통해 작물/영농일지/작업 일정 목록을 로컬에 캐시하여, 네트워크가 불안정한 환경에서도 최근 조회한 데이터를 확인할 수 있습니다.
