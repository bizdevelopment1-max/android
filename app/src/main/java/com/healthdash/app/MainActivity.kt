package com.healthdash.app

import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.healthdash.app.ui.AiSelectionBar
import com.healthdash.app.ui.BookmarkSheet
import com.healthdash.app.ui.BottomNavBar
import com.healthdash.app.ui.CollapsedBarHandle
import com.healthdash.app.ui.FabGroup
import com.healthdash.app.ui.HealthDashTheme
import com.healthdash.app.ui.HistorySheet
import com.healthdash.app.ui.SearchOverlay
import com.healthdash.app.ui.SettingsSheet
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()
    private lateinit var appSettings: SettingsManager
    private var dashWebView: WebView? = null
    private var ttsManager: TtsManager? = null
    private var lastSearchQuery: String = ""
    private var loadRetryCount = 0
    private var blankRetryCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appSettings = SettingsManager(this)
        ttsManager = TtsManager(this)
        registerNetworkCallback()
        setContent {
            HealthDashTheme {
                MainScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        val textZoom by vm.textZoom.collectAsState()
        val selectedText by vm.selectedText.collectAsState()
        val activeSection by vm.activeSection.collectAsState()
        val isLoading by vm.isLoading.collectAsState()
        val isOffline by vm.isOffline.collectAsState()
        val bookmarks by vm.bookmarks.collectAsState()
        val showSearch by vm.showSearch.collectAsState()
        val showBookmarks by vm.showBookmarks.collectAsState()
        val showSettings by vm.showSettings.collectAsState()
        val showHistory by vm.showHistory.collectAsState()
        val aiHistory by vm.aiHistory.collectAsState()
        val highContrast by vm.highContrast.collectAsState()
        val ttsSpeed by vm.ttsSpeed.collectAsState()
        val keywords by vm.keywords.collectAsState()
        val barVisible by vm.barVisible.collectAsState()
        val barScale by vm.barScale.collectAsState()
        val dark = isSystemInDarkTheme()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(textZoom) {
            dashWebView?.settings?.textZoom = textZoom
        }
        LaunchedEffect(dark) {
            dashWebView?.let { WebViewManager.injectTheme(it, dark) }
        }
        LaunchedEffect(isOffline) {
            if (isOffline) snackbarHostState.showSnackbar("오프라인 — 캐시 데이터 표시 중")
        }

        BackHandler(enabled = showSearch) {
            vm.setShowSearch(false)
        }

        Box(Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    AndroidView(
                        factory = { ctx -> createDashboardView(ctx) },
                        modifier = Modifier.fillMaxSize()
                    )
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                    FabGroup(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 10.dp),
                        onZoomIn = { vm.setTextZoom(vm.textZoom.value + 10) },
                        onZoomOut = { vm.setTextZoom(vm.textZoom.value - 10) },
                        onScrollUp = { dashWebView?.let { WebViewManager.scrollPage(it, -0.35) } },
                        onScrollDown = { dashWebView?.let { WebViewManager.scrollPage(it, 0.35) } },
                        onPageUp = { dashWebView?.let { WebViewManager.scrollPage(it, -0.92) } },
                        onPageDown = { dashWebView?.let { WebViewManager.scrollPage(it, 0.92) } },
                        onRefresh = { reloadDashboard() }
                    )
                    // 반투명 플로팅 하단 영역: AI 선택 바 + 접을 수 있는 내비 바
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AiSelectionBar(
                            modifier = Modifier.fillMaxWidth(),
                            selectedText = selectedText,
                            onAiClick = { app -> openAiApp(app, vm.selectedText.value) },
                            onAiLongClick = { app -> shareToApps(app, vm.selectedText.value) },
                            onTts = { ttsManager?.speak(vm.selectedText.value, vm.ttsSpeed.value) },
                            onTranslate = { translateSelected(vm.selectedText.value) },
                            onHighlight = {
                                val text = vm.selectedText.value
                                vm.addHighlight(text)
                                dashWebView?.let { WebViewManager.injectHighlight(it, text) }
                                Toast.makeText(this@MainActivity, "하이라이트 저장됨", Toast.LENGTH_SHORT).show()
                            }
                        )
                        if (barVisible) {
                            BottomNavBar(
                                activeSection = activeSection,
                                barScale = barScale,
                                onTabClick = { tab ->
                                    vm.setActiveSection(tab.id)
                                    dashWebView?.let { WebViewManager.navigateToSection(it, tab.id, tab.label) }
                                },
                                onMoveSection = { delta ->
                                    val tab = vm.moveSection(delta)
                                    dashWebView?.let { WebViewManager.navigateToSection(it, tab.id, tab.label) }
                                },
                                onAiClick = { app -> quickLaunchAi(app) },
                                onSettingsClick = { vm.setShowSettings(true) },
                                onCollapse = { vm.setBarVisible(false) },
                                onScaleDrag = { delta -> vm.setBarScale(vm.barScale.value + delta) }
                            )
                        } else {
                            CollapsedBarHandle(onExpand = { vm.setBarVisible(true) })
                        }
                    }
                }
            }

            if (showSearch) {
                SearchOverlay(
                    onQueryChange = { query ->
                        lastSearchQuery = query
                        dashWebView?.let { WebViewManager.search(it, query) }
                    },
                    onNext = { dashWebView?.findNext(true) },
                    onPrev = { dashWebView?.findNext(false) },
                    onDismiss = {
                        lastSearchQuery = ""
                        dashWebView?.clearMatches()
                        vm.setShowSearch(false)
                    }
                )
            }
        }

        if (showBookmarks) {
            BookmarkSheet(
                bookmarks = bookmarks,
                onSelect = { bookmark ->
                    vm.setActiveSection(bookmark.sectionId)
                    dashWebView?.let { WebViewManager.navigateToSection(it, bookmark.sectionId, bookmark.label) }
                    vm.setShowBookmarks(false)
                },
                onDelete = { vm.deleteBookmark(it) },
                onAddCurrent = {
                    vm.addCurrentBookmark()
                    Toast.makeText(this@MainActivity, "북마크 저장됨", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { vm.setShowBookmarks(false) }
            )
        }

        if (showSettings) {
            SettingsSheet(
                textZoom = textZoom,
                onPreset = { vm.setTextZoom(it) },
                highContrast = highContrast,
                onHighContrast = { enabled ->
                    vm.setHighContrast(enabled)
                    dashWebView?.let { WebViewManager.injectHighContrast(it, enabled) }
                },
                ttsSpeed = ttsSpeed,
                onTtsSpeed = { vm.setTtsSpeed(it) },
                barScale = barScale,
                onBarScale = { vm.setBarScale(it) },
                keywords = keywords,
                onKeywords = { vm.setKeywords(it) },
                onOpenSearch = {
                    vm.setShowSettings(false)
                    vm.setShowSearch(true)
                },
                onShowBookmarks = {
                    vm.setShowSettings(false)
                    vm.setShowBookmarks(true)
                },
                onShowHistory = {
                    vm.setShowSettings(false)
                    vm.setShowHistory(true)
                },
                onRefresh = {
                    vm.setShowSettings(false)
                    dashWebView?.reload()
                },
                onShare = {
                    vm.setShowSettings(false)
                    sharePage(vm.selectedText.value)
                },
                onScreenshot = {
                    vm.setShowSettings(false)
                    captureWebView()
                },
                onDismiss = { vm.setShowSettings(false) }
            )
        }

        if (showHistory) {
            HistorySheet(historyItems = aiHistory, onDismiss = { vm.setShowHistory(false) })
        }
    }

    /** 대시보드 WebView 생성 (당겨서 새로고침 없음 — 새로고침은 설정 시트에서) */
    private fun createDashboardView(ctx: android.content.Context): WebView {
        val wv = WebViewManager.createDashboardWebView(
            context = this,
            appSettings = appSettings,
            onTextSelected = { text -> vm.setSelectedText(text) },
            onSectionVisible = { id -> vm.setActiveSection(id) },
            onKeywordFound = { keywords ->
                runOnUiThread {
                    Toast.makeText(this, "관심 키워드 발견: $keywords", Toast.LENGTH_LONG).show()
                }
            },
            onProgress = { progress -> vm.setLoading(progress in 1..99) },
            onPageFinished = { web ->
                loadRetryCount = 0
                WebViewManager.injectInitScript(web)
                WebViewManager.injectTheme(web, isDarkMode())
                WebViewManager.injectHighContrast(web, vm.highContrast.value)
                WebViewManager.injectKeywordCheck(web, vm.keywords.value)
                vm.highlights.value.forEach { WebViewManager.injectHighlight(web, it.text) }
                web.postDelayed({ checkBlankAndRecover(web) }, 3000)
            },
            onMainFrameError = {
                runOnUiThread {
                    if (loadRetryCount < 2) {
                        loadRetryCount++
                        dashWebView?.postDelayed({
                            if (!vm.isOffline.value) reloadDashboard()
                        }, 2500)
                    } else {
                        Toast.makeText(
                            this,
                            "페이지 로드 실패 — 우측 새로고침 버튼을 눌러주세요",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
        WebViewManager.attachPinchZoom(
            wv,
            getZoom = { vm.textZoom.value },
            setZoom = { zoom -> vm.setTextZoom(zoom) }
        )
        wv.setFindListener { _, numberOfMatches, isDoneCounting ->
            if (isDoneCounting && numberOfMatches == 0 && lastSearchQuery.isNotBlank()) {
                Toast.makeText(this, "검색 결과 없음", Toast.LENGTH_SHORT).show()
            }
        }
        WebViewManager.setOfflineMode(wv, vm.isOffline.value)
        dashWebView = wv

        // 첫 로드는 WebView가 실제 크기로 배치된 뒤 시작 —
        // 폭 0 상태에서 로드하면 SPA가 빈 화면(흰 화면)으로 렌더링되는 문제 방지
        var initialLoadDone = false
        wv.addOnLayoutChangeListener(object : android.view.View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: android.view.View,
                left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                if (!initialLoadDone && v.width > 0) {
                    initialLoadDone = true
                    v.removeOnLayoutChangeListener(this)
                    wv.loadUrl(WebViewManager.DASHBOARD_URL)
                }
            }
        })
        // 안전망: 레이아웃 이벤트가 오지 않으면 1.5초 후 그냥 로드
        wv.postDelayed({
            if (!initialLoadDone) {
                initialLoadDone = true
                wv.loadUrl(WebViewManager.DASHBOARD_URL)
            }
        }, 1500)
        // 워치독: 10초가 지나도 로드가 거의 진행되지 않았으면 자동 재시도
        wv.postDelayed({
            if (!isDestroyed && !isFinishing && (wv.url.isNullOrBlank() || wv.progress < 30)) {
                reloadDashboard()
            }
        }, 10000)
        return wv
    }

    /** 로드 완료 후에도 본문이 비어 있으면(흰 화면) 자동 재로드 */
    private fun checkBlankAndRecover(web: WebView) {
        web.evaluateJavascript(
            "(function(){try{return document.body&&document.body.innerText?document.body.innerText.trim().length:0}catch(e){return 0}})()"
        ) { value ->
            val len = value?.replace("\"", "")?.toIntOrNull() ?: 0
            if (len < 40) {
                if (blankRetryCount < 2 && !vm.isOffline.value) {
                    blankRetryCount++
                    reloadDashboard()
                }
            } else {
                blankRetryCount = 0
            }
        }
    }

    /** 대시보드 새로고침 — URL이 비어 있으면(최초 로드 실패) 처음부터 다시 로드 */
    private fun reloadDashboard() {
        val wv = dashWebView ?: return
        val url = wv.url
        if (url.isNullOrBlank() || url == "about:blank") {
            wv.loadUrl(WebViewManager.DASHBOARD_URL)
        } else {
            wv.reload()
        }
    }

    /** 하단 바 AI 로고 탭 — 선택 텍스트가 있으면 함께 전달, 없으면 앱만 실행 */
    private fun quickLaunchAi(app: AiApp) {
        val text = vm.selectedText.value
        vm.recordAiSend(app, text)
        AiBarManager.launchExternalApp(this, app, text)
    }

    /** AI 버튼 탭 — 로그인된 네이티브 AI 앱을 분할 화면으로 실행하고 텍스트 전달 */
    private fun openAiApp(app: AiApp, text: String) {
        if (text.isBlank()) {
            Toast.makeText(this, "먼저 본문에서 텍스트를 선택하세요", Toast.LENGTH_SHORT).show()
            return
        }
        vm.recordAiSend(app, text)
        val installed = AiBarManager.isInstalled(this, app.packageName)
        AiBarManager.launchExternalApp(this, app, text)
        if (installed && !isInMultiWindowMode) {
            Toast.makeText(
                this,
                "${app.displayName} 실행 — 분할 화면을 원하면 최근 앱(□) 버튼에서 '화면 분할'을 선택하세요",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /** AI 버튼 길게 누름 — 공유 시트로 원하는 앱 직접 선택 */
    private fun shareToApps(app: AiApp, text: String) {
        if (text.isBlank()) {
            Toast.makeText(this, "먼저 본문에서 텍스트를 선택하세요", Toast.LENGTH_SHORT).show()
            return
        }
        AiBarManager.copyToClipboard(this, text)
        vm.recordAiSend(app, text)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "${app.displayName} 등 앱으로 보내기"))
    }

    private fun sharePage(selected: String) {
        val text = buildString {
            append(WebViewManager.DASHBOARD_URL)
            if (selected.isNotBlank()) {
                append("\n\n")
                append(selected.take(500))
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "공유"))
    }

    private fun translateSelected(text: String) {
        if (text.isBlank()) return
        lifecycleScope.launch {
            val result = AiBarManager.translate(text)
            Toast.makeText(
                this@MainActivity,
                result ?: "번역에 실패했습니다 (네트워크 확인)",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /** 현재 WebView를 비트맵으로 캡처해 MediaStore(갤러리)에 저장 */
    private fun captureWebView() {
        val wv = dashWebView ?: return
        if (wv.width <= 0 || wv.height <= 0) return
        try {
            val bitmap = Bitmap.createBitmap(wv.width, wv.height, Bitmap.Config.ARGB_8888)
            wv.draw(Canvas(bitmap))
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "HealthDash_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/HealthDash")
                }
            }
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Toast.makeText(this, "스크린샷 저장됨 — 갤러리 > Pictures/HealthDash", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "스크린샷 저장 실패", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "스크린샷 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isDarkMode(): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    private fun registerNetworkCallback() {
        try {
            val cm = getSystemService(ConnectivityManager::class.java) ?: return
            vm.setOffline(cm.activeNetwork == null)
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    vm.setOffline(false)
                    runOnUiThread { dashWebView?.let { WebViewManager.setOfflineMode(it, false) } }
                }

                override fun onLost(network: Network) {
                    vm.setOffline(true)
                    runOnUiThread { dashWebView?.let { WebViewManager.setOfflineMode(it, true) } }
                }
            })
        } catch (_: Exception) {
        }
    }

    /** 볼륨 업 키로 검색 오버레이 열기 */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && !vm.showSearch.value) {
            vm.setShowSearch(true)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        // 최초 로드가 실패한 채 복귀했으면 다시 시도
        val wv = dashWebView
        if (wv != null && (wv.url.isNullOrBlank() || wv.url == "about:blank")) {
            wv.loadUrl(WebViewManager.DASHBOARD_URL)
        }
    }

    override fun onDestroy() {
        ttsManager?.shutdown()
        ttsManager = null
        super.onDestroy()
    }
}
