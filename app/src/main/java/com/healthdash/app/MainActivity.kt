package com.healthdash.app

import android.content.ContentValues
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.ViewGroup
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.healthdash.app.ui.AiPanelTopBar
import com.healthdash.app.ui.AiSelectionBar
import com.healthdash.app.ui.BookmarkSheet
import com.healthdash.app.ui.BottomNavBar
import com.healthdash.app.ui.FabGroup
import com.healthdash.app.ui.HealthDashTheme
import com.healthdash.app.ui.HistorySheet
import com.healthdash.app.ui.SearchOverlay
import com.healthdash.app.ui.SettingsSheet
import com.healthdash.app.ui.SplitViewHandle
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()
    private lateinit var appSettings: SettingsManager
    private var dashWebView: WebView? = null
    private var aiWebView: WebView? = null
    private var ttsManager: TtsManager? = null
    private var lastSearchQuery: String = ""

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
        val isSplit by vm.isSplitMode.collectAsState()
        val splitRatio by vm.splitRatio.collectAsState()
        val selectedText by vm.selectedText.collectAsState()
        val activeSection by vm.activeSection.collectAsState()
        val currentAiApp by vm.currentAiApp.collectAsState()
        val pendingAiUrl by vm.pendingAiUrl.collectAsState()
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
        val dark = isSystemInDarkTheme()
        val snackbarHostState = remember { SnackbarHostState() }
        var containerHeightPx by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(textZoom) {
            dashWebView?.settings?.textZoom = textZoom
        }
        LaunchedEffect(dark) {
            dashWebView?.let { WebViewManager.injectTheme(it, dark) }
        }
        LaunchedEffect(isOffline) {
            if (isOffline) snackbarHostState.showSnackbar("오프라인 — 캐시 데이터 표시 중")
        }

        BackHandler(enabled = isSplit || showSearch) {
            when {
                showSearch -> vm.setShowSearch(false)
                isSplit -> vm.closeSplit()
            }
        }

        Box(Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    BottomNavBar(
                        activeSection = activeSection,
                        onTabClick = { tab ->
                            vm.setActiveSection(tab.id)
                            dashWebView?.let { WebViewManager.navigateToSection(it, tab.id) }
                        },
                        onMoveSection = { delta ->
                            val id = vm.moveSection(delta)
                            dashWebView?.let { WebViewManager.navigateToSection(it, id) }
                        },
                        onBookmarkClick = { vm.setShowBookmarks(true) },
                        onSearchClick = { vm.setShowSearch(true) },
                        onSettingsClick = { vm.setShowSettings(true) }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .onSizeChanged { containerHeightPx = it.height.toFloat() }
                ) {
                    if (isSplit) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .weight(splitRatio)
                        ) {
                            AiPanelTopBar(
                                current = currentAiApp,
                                onSwitch = { app -> vm.switchAiApp(app) },
                                onHistory = { vm.setShowHistory(true) }
                            )
                            AndroidView(
                                factory = { ctx ->
                                    WebViewManager.createAiWebView(ctx) { vm.lastAiText }
                                        .also { aiWebView = it }
                                },
                                update = { wv ->
                                    if (pendingAiUrl.isNotEmpty() && wv.tag != pendingAiUrl) {
                                        wv.tag = pendingAiUrl
                                        wv.loadUrl(pendingAiUrl)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                        SplitViewHandle(
                            onDrag = { dy ->
                                vm.setSplitRatio(
                                    SplitViewController.applyDrag(vm.splitRatio.value, dy, containerHeightPx)
                                )
                            },
                            onDoubleTap = { vm.resetSplitRatio() },
                            onClose = { vm.closeSplit() }
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(if (isSplit) 1f - splitRatio else 1f)
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
                                .align(Alignment.BottomEnd)
                                .padding(end = 12.dp, bottom = 16.dp),
                            onZoomIn = { vm.setTextZoom(vm.textZoom.value + 10) },
                            onZoomOut = { vm.setTextZoom(vm.textZoom.value - 10) },
                            onRotate = { toggleOrientation() },
                            onShare = { sharePage(vm.selectedText.value) },
                            onScreenshot = { captureWebView() }
                        )
                        AiSelectionBar(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(),
                            selectedText = selectedText,
                            onAiClick = { app -> openAiInSplit(app, vm.selectedText.value) },
                            onAiLongClick = { app ->
                                AiBarManager.launchExternalApp(this@MainActivity, app, vm.selectedText.value)
                            },
                            onTts = { ttsManager?.speak(vm.selectedText.value, vm.ttsSpeed.value) },
                            onTranslate = { translateSelected(vm.selectedText.value) },
                            onHighlight = {
                                val text = vm.selectedText.value
                                vm.addHighlight(text)
                                dashWebView?.let { WebViewManager.injectHighlight(it, text) }
                                Toast.makeText(this@MainActivity, "하이라이트 저장됨", Toast.LENGTH_SHORT).show()
                            }
                        )
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
                    dashWebView?.let { WebViewManager.navigateToSection(it, bookmark.sectionId) }
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
                keywords = keywords,
                onKeywords = { vm.setKeywords(it) },
                onDismiss = { vm.setShowSettings(false) }
            )
        }

        if (showHistory) {
            HistorySheet(items = aiHistory, onDismiss = { vm.setShowHistory(false) })
        }
    }

    /** 대시보드 WebView + PullToRefresh 컨테이너 생성 */
    private fun createDashboardView(ctx: android.content.Context): SwipeRefreshLayout {
        val swipe = SwipeRefreshLayout(ctx)
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
                swipe.isRefreshing = false
                WebViewManager.injectInitScript(web)
                WebViewManager.injectTheme(web, isDarkMode())
                WebViewManager.injectHighContrast(web, vm.highContrast.value)
                WebViewManager.injectKeywordCheck(web, vm.keywords.value)
                vm.highlights.value.forEach { WebViewManager.injectHighlight(web, it.text) }
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
        dashWebView = wv
        swipe.addView(
            wv,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        swipe.setOnRefreshListener { wv.reload() }
        wv.loadUrl(WebViewManager.DASHBOARD_URL)
        return swipe
    }

    /** AI 버튼 탭 — 클립보드 복사 후 스플릿 뷰에 해당 AI 웹 열기 */
    private fun openAiInSplit(app: AiApp, text: String) {
        if (text.isBlank()) {
            Toast.makeText(this, "먼저 본문에서 텍스트를 선택하세요", Toast.LENGTH_SHORT).show()
            return
        }
        AiBarManager.copyToClipboard(this, text)
        vm.openSplit(app, text)
        Toast.makeText(this, "클립보드에 복사됨 — ${app.displayName} 입력창에 자동 입력을 시도합니다", Toast.LENGTH_SHORT).show()
    }

    private fun toggleOrientation() {
        val landscape = vm.toggleLandscape()
        requestedOrientation = if (landscape) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
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
                }

                override fun onLost(network: Network) {
                    vm.setOffline(true)
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

    override fun onDestroy() {
        ttsManager?.shutdown()
        ttsManager = null
        super.onDestroy()
    }
}
