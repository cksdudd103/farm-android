# SmartFarm WebApp 배포 가이드

## 개요

이 프로젝트는 React + Vite 프론트엔드와 Express 정적 서버(`server.cjs`)로 구성되어 있습니다. 모든 사용자 데이터(회원, 작물, 일지, 작업, 재고, 진단)는 백엔드(Flask, `farm-webapp` 저장소, PostgreSQL)에 저장됩니다. 프론트엔드는 `axios`로 백엔드 REST API(`https://farm-webapp-rezy.onrender.com`)를 직접 호출하며, 로그인 세션은 cross-origin 쿠키(`SameSite=None; Secure`)로 유지됩니다.

## 빌드

```bash
cd webapp
npm install
npm run build
```

빌드 결과는 `webapp/dist` 폴더에 생성됩니다.

## 1. Render 물리 배포 (추천)

### Web Service로 생성

1. [Render Dashboard](https://dashboard.render.com) 접속
2. **New +** → **Web Service**
3. GitHub 저장소 연결
4. 설정:
   - **Name**: `farm-webapp`
   - **Environment**: `Node`
   - **Build Command**: `cd webapp && npm install && npm run build`
   - **Start Command**: `cd webapp && node server.cjs`
   - **Plan**: Free
5. **Deploy** 클릭

주의: Render Free는 미사용 시 sleep되며 첫 접속에 지연이 있습니다.

## 2. Railway 배포

1. [Railway](https://railway.app) 가입
2. New Project → GitHub Repo
3. 변수 설정:
   - `RAILWAY_DOCKERFILE_PATH` 필요 없음
4. Start Command: `cd webapp && node server.cjs`
5. Domain 자동 할당

## 3. VPS/자체 서버 배포

서버에 Node.js 18+ 설치 후:

```bash
git clone <your-repo>
cd farm-android/webapp
npm install
npm run build
node server.cjs
```

백그라운드 실행:

```bash
npm install -g pm2
pm2 start server.cjs --name farm-webapp
pm2 save
pm2 startup
```

## 4. Netlify/Cloudflare Pages (정적 사이트만)

이 방법은 `/api/weather`, `/api/external-links` 등 Express 프록시 기능을 사용할 수 없습니다. 따라서 추천하지 않습니다.

## 환경 변수

필수 환경 변수는 없습니다. 선택사항:

```env
VITE_API_BASE_URL=https://your-backend.example.com/api
PORT=3000
```

`VITE_API_BASE_URL`을 설정하면 Express 프록시가 해당 URL로 요청을 전달합니다. 설정하지 않으면 `https://farm-webapp-rezy.onrender.com/api`로 프록시됩니다.

## 관리자 계정 생성

회원가입 시 **관리자 코드**에 `영농102`를 입력하면 관리자(`admin`) 권한으로 즉시 가입됩니다. 코드를 입력하지 않으면 일반 `farmer` 역할로 가입되며, 별도 승인 절차 없이 바로 로그인할 수 있습니다.

## 데이터 저장

모든 데이터는 백엔드(`farm-webapp`, PostgreSQL)에 저장됩니다. 브라우저나 기기를 바꿔도 로그인만 하면 동일한 데이터가 보입니다.

## 주의사항

- 백엔드가 sleep 상태(Render Free 플랜)이면 첫 요청이 지연될 수 있습니다.
- 프론트엔드와 백엔드가 서로 다른 도메인이므로 로그인 세션 쿠키는 `SameSite=None; Secure`로 설정되어 있어야 하며, 반드시 HTTPS 환경에서 접속해야 정상 동작합니다.

## 로컬 실행

```bash
cd webapp
npm install
npm run build
node server.cjs
```

브라우저에서 `http://localhost:3000` 접속

## 문제 해결

### `node server.cjs` 실행 후 접속이 안 됨

- 다른 프로세스가 3000번 포트를 사용 중인지 확인: `netstat -ano | findstr :3000`
- pm2 사용 권장

### 빌드 실패

```bash
cd webapp
rm -rf node_modules dist package-lock.json
npm install
npm run build
```

### KAMIS/기상청 실시간 데이터가 안 나옴

해당 서비스는 공공 API 인증키가 필요합니다. 현재는 샘플 데이터 + 공식 사이트 링크로 대체 제공됩니다.
