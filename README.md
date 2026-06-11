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
- **센서 자동 회전**(fullSensor), **고대비 모드**(흰 배경 + 검정 글자 강제 CSS), **다크모드 싱크**(`nativeTheme` 커스텀 이벤트 전달)
- **당겨서 새로고침**, 오프라인 시 캐시 표시 + 스낵바 안내

### 텍스트 선택 → AI 4종 (네이티브 앱 분할 실행)
1. 본문에서 텍스트를 길게 눌러 선택하면 하단에 AI 선택 바가 슬라이드업
2. ChatGPT / Gemini / Claude / Perplexity 버튼 **탭** → **로그인되어 있는 네이티브 AI 앱**을
   분할 화면(`FLAG_ACTIVITY_LAUNCH_ADJACENT`)으로 실행하고 텍스트를 전달
   - 전달 순서: ①`ACTION_SEND` 공유 인텐트(텍스트가 앱에 바로 들어감) → ②앱 딥링크(`?q=…`) → ③앱 실행 + 클립보드 붙여넣기
   - 앱 미설치 시 웹 URL을 외부 브라우저로 오픈
3. 버튼을 **길게 누르면** 공유 시트가 떠서 원하는 앱을 직접 선택 가능
4. 어떤 경로든 텍스트는 항상 클립보드에 복사되므로 길게 눌러 붙여넣기 가능

> 참고: Android는 일반 앱이 분할 화면을 강제로 켤 수 없습니다.
> `LAUNCH_ADJACENT`는 이미 분할 화면 상태이거나 기기가 지원할 때 옆/위 창으로 열리며,
> 아닐 경우 일반 실행됩니다 (이때 최근 앱(□) 버튼 → "화면 분할"을 선택하면 됩니다).
> AI 전송 히스토리는 설정(⚙) → "AI 전송 히스토리 보기"에서 확인할 수 있습니다.

### 내비게이션 (하단 바)
- 12개 섹션 탭 + AI 4사 로고 버튼 + 설정이 **모두 함께 슬라이드**, ◀ ▶ 화살표만 양끝 고정(한 칸씩 이동)
- 탭별 고유 색상 + 선택 시 필(pill) 배경·스프링 바운스·색상 전환 애니메이션
- AI 로고 버튼: 선택 텍스트가 있으면 함께 전달, 없으면 해당 AI 앱 바로 실행
- 스크롤 스파이(IntersectionObserver)로 현재 섹션 하이라이트 동기화
- 북마크·검색·AI 히스토리는 설정(⚙) 시트에서 진입 (검색은 볼륨 업 키로도 열림)

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
  MainActivity.kt        — 메인 화면, AI 앱 실행/공유/스크린샷
  MainViewModel.kt       — StateFlow 상태 (줌/선택 텍스트/북마크 등)
  WebViewManager.kt      — WebView 초기화, JS Bridge, 주입 스크립트, 핀치 줌
  AiBarManager.kt        — AiApp enum, 클립보드/네이티브 앱 분할 실행/번역/TTS
  BookmarkRepository.kt  — Room CRUD
  SettingsManager.kt     — SharedPreferences 래퍼
  ui/                    — Compose UI (탭 바, FAB, AI 바, 핸들, 검색, 시트)
  data/                  — Room DB / Entity
```
