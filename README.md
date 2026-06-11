# BD Health — 헬스케어 대시보드 리더 앱

[https://bizdevelopment1-max.github.io/health/](https://bizdevelopment1-max.github.io/health/) 대시보드를
모바일에서 읽기 좋게 보여주는 Android 앱입니다. 단순 WebView 래퍼가 아니라,
**선택한 텍스트를 ChatGPT / Gemini / Claude / Perplexity로 바로 보내 질문할 수 있는 읽기 보조 + AI 리서치 도구**입니다.

## APK 다운로드

GitHub Actions가 푸시마다 APK를 자동 빌드합니다.

- **최신 APK**: [Releases → apk-latest](../../releases/tag/apk-latest) 에서 `BDHealth-debug.apk` 다운로드
- 또는 [Actions](../../actions) 탭 → 최신 `Build APK` 실행 → Artifacts → `BDHealth-debug-apk`

설치 시 "출처를 알 수 없는 앱 설치 허용"이 필요할 수 있습니다 (디버그 서명 APK).

## 주요 기능

### 읽기 / 가독성
- **텍스트 줌**: A+ / A- FAB(±10%), 핀치 줌 제스처, 글꼴 프리셋(70/85/100/120/150) — 재시작 후에도 유지
- **화면 회전 토글**, **고대비 모드**(흰 배경 + 검정 글자 강제 CSS), **다크모드 싱크**(`nativeTheme` 커스텀 이벤트 전달)
- **당겨서 새로고침**, 오프라인 시 캐시 표시 + 스낵바 안내

### 텍스트 선택 → AI 4종
1. 본문에서 텍스트를 길게 눌러 선택하면 하단에 AI 선택 바가 슬라이드업
2. ChatGPT / Gemini / Claude / Perplexity 버튼 **탭** → 클립보드 복사 + **상하 스플릿 뷰**에 해당 AI 웹을 열고
   선택 텍스트를 쿼리 파라미터(`?q=…`)로 전달, 입력창 자동 입력도 시도
3. 버튼을 **길게 누르면** 설치된 네이티브 AI 앱으로 전송 (`ACTION_SEND` + 스플릿 스크린 시도, 미설치 시 웹으로)
4. 자동 입력이 실패해도 텍스트는 항상 클립보드에 있으므로 길게 눌러 붙여넣기 가능

> 참고: 타 앱 입력창에 텍스트를 강제로 넣는 것은 Android 정책상 보장되지 않습니다.
> 이 앱은 ①인앱 WebView 자동 입력 시도 → ②클립보드 복사 → ③공유 인텐트의 3단계 fallback으로 동작합니다.

### 스플릿 뷰
- 상단 AI 패널(기본 40%) + 드래그 핸들(24dp) + 하단 대시보드(60%)
- 드래그로 25~75% 실시간 조절, 더블탭으로 40/60 복원, X로 닫기, 마지막 비율 저장
- 상단 탭으로 4개 AI 전환, 전송 히스토리 타임라인 제공

### 내비게이션
- 하단 가로 스크롤 탭 바(12개 섹션) + **◀ ▶ 화살표로 한 칸씩 이동**
- 스크롤 스파이(IntersectionObserver)로 현재 섹션 하이라이트 동기화
- 북마크(Room DB 저장, 바텀시트), 검색 오버레이(볼륨 업 키로도 열림)

### 부가 기능
- 하이라이트 저장(노란 형광펜, 재시작 시 복원), TTS 읽어주기(0.5x~2.0x), 한↔영 번역,
  관심 키워드 알림, 스크린샷 갤러리 저장, 페이지/선택 텍스트 공유

## 빌드 방법

```bash
./gradlew assembleDebug
# 출력: app/build/outputs/apk/debug/app-debug.apk
```

- 패키지: `com.healthdash.app` / minSdk 26 / targetSdk 35
- Kotlin + Jetpack Compose(Material 3, LG Blue `#1428A0`), Room, androidx.webkit

## 구조

```
app/src/main/java/com/healthdash/app/
  MainActivity.kt        — 메인 화면, 스플릿 뷰 상태 관리
  MainViewModel.kt       — StateFlow 상태 (줌/스플릿/선택 텍스트/북마크 등)
  WebViewManager.kt      — WebView 초기화, JS Bridge, 주입 스크립트, 핀치 줌
  SplitViewController.kt — 스플릿 비율 계산
  AiBarManager.kt        — AiApp enum, 클립보드/외부 앱/번역/TTS
  BookmarkRepository.kt  — Room CRUD
  SettingsManager.kt     — SharedPreferences 래퍼
  ui/                    — Compose UI (탭 바, FAB, AI 바, 핸들, 검색, 시트)
  data/                  — Room DB / Entity
```
