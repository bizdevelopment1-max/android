package com.healthdash.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthdash.app.data.AppDatabase
import com.healthdash.app.data.BookmarkEntity
import com.healthdash.app.data.HighlightEntity
import com.healthdash.app.ui.NAV_TABS
import com.healthdash.app.ui.NavTab
import com.healthdash.app.ui.buildNavTabs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AiHistoryItem(
    val app: AiApp,
    val text: String,
    val time: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = SettingsManager(application)
    private val repo = BookmarkRepository(AppDatabase.get(application))

    private val _textZoom = MutableStateFlow(settings.textZoom)
    val textZoom: StateFlow<Int> = _textZoom.asStateFlow()

    private val _activeSection = MutableStateFlow(NAV_TABS.first().id)
    val activeSection: StateFlow<String> = _activeSection.asStateFlow()

    private val _selectedText = MutableStateFlow("")
    val selectedText: StateFlow<String> = _selectedText.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showSearch = MutableStateFlow(false)
    val showSearch: StateFlow<Boolean> = _showSearch.asStateFlow()

    private val _showBookmarks = MutableStateFlow(false)
    val showBookmarks: StateFlow<Boolean> = _showBookmarks.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _showHistory = MutableStateFlow(false)
    val showHistory: StateFlow<Boolean> = _showHistory.asStateFlow()

    private val _highContrast = MutableStateFlow(settings.highContrast)
    val highContrast: StateFlow<Boolean> = _highContrast.asStateFlow()

    private val _ttsSpeed = MutableStateFlow(settings.ttsSpeed)
    val ttsSpeed: StateFlow<Float> = _ttsSpeed.asStateFlow()

    private val _keywords = MutableStateFlow(settings.keywords)
    val keywords: StateFlow<String> = _keywords.asStateFlow()

    private val _barVisible = MutableStateFlow(true)
    val barVisible: StateFlow<Boolean> = _barVisible.asStateFlow()

    private val _barScale = MutableStateFlow(settings.barScale)
    val barScale: StateFlow<Float> = _barScale.asStateFlow()

    // 사이트 왼쪽 내비에서 추출한 탭 (추출 전에는 기본 AI 인사이트 탭으로 폴백)
    private val _navTabs = MutableStateFlow(NAV_TABS)
    val navTabs: StateFlow<List<NavTab>> = _navTabs.asStateFlow()

    // 테마: 0 시스템 / 1 라이트 / 2 다크 (저장되어 재시작 시 유지)
    private val _themeMode = MutableStateFlow(settings.themeMode)
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    // 런치 페이지 컬러 테마(0~5, 저장) + 런치 페이지 표시 여부(콜드 스타트마다 표시)
    private val _launchTheme = MutableStateFlow(settings.launchTheme)
    val launchTheme: StateFlow<Int> = _launchTheme.asStateFlow()

    private val _showLaunch = MutableStateFlow(true)
    val showLaunch: StateFlow<Boolean> = _showLaunch.asStateFlow()

    private val _aiHistory = MutableStateFlow<List<AiHistoryItem>>(emptyList())
    val aiHistory: StateFlow<List<AiHistoryItem>> = _aiHistory.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>> =
        repo.bookmarks.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val highlights: StateFlow<List<HighlightEntity>> =
        repo.highlights.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setTextZoom(value: Int) {
        val v = value.coerceIn(SettingsManager.MIN_ZOOM, SettingsManager.MAX_ZOOM)
        _textZoom.value = v
        settings.textZoom = v
    }

    fun setActiveSection(id: String) {
        if (id.isNotBlank()) _activeSection.value = id
    }

    /** 스크롤 스파이 — 현재 탭에 존재하는 id일 때만 하이라이트 갱신(잘못된 하이라이트 방지) */
    fun onScrollSpySection(id: String) {
        if (id.isNotBlank() && _navTabs.value.any { it.id == id }) {
            _activeSection.value = id
        }
    }

    fun setSelectedText(text: String) {
        _selectedText.value = text
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun setOffline(offline: Boolean) {
        _isOffline.value = offline
    }

    fun setShowSearch(show: Boolean) {
        _showSearch.value = show
    }

    fun setShowBookmarks(show: Boolean) {
        _showBookmarks.value = show
    }

    fun setShowSettings(show: Boolean) {
        _showSettings.value = show
    }

    fun setShowHistory(show: Boolean) {
        _showHistory.value = show
    }

    fun setHighContrast(enabled: Boolean) {
        _highContrast.value = enabled
        settings.highContrast = enabled
    }

    fun setTtsSpeed(speed: Float) {
        _ttsSpeed.value = speed
        settings.ttsSpeed = speed
    }

    fun setKeywords(keywords: String) {
        _keywords.value = keywords
        settings.keywords = keywords
    }

    fun setBarVisible(visible: Boolean) {
        _barVisible.value = visible
    }

    fun setBarScale(scale: Float) {
        val s = scale.coerceIn(0.7f, 1.4f)
        _barScale.value = s
        settings.barScale = s
    }

    fun setThemeMode(mode: Int) {
        val m = mode.coerceIn(0, 2)
        _themeMode.value = m
        settings.themeMode = m
    }

    fun setLaunchTheme(index: Int) {
        val i = index.coerceIn(0, 5)
        _launchTheme.value = i
        settings.launchTheme = i
    }

    fun dismissLaunch() {
        _showLaunch.value = false
    }

    /** 사이트에서 추출한 내비 라벨로 하단 탭을 교체. 기존과 같으면 무시. */
    /** 하단 탭은 사이드바에 맞춘 고정 11개를 사용하므로 동적 추출은 무시(덮어쓰지 않음). */
    fun setNavLabels(labels: List<String>) {
        // no-op
    }

    /** AI 앱으로 보낸 텍스트를 히스토리에 기록 */
    fun recordAiSend(app: AiApp, text: String) {
        if (text.isBlank()) return
        _aiHistory.value = _aiHistory.value + AiHistoryItem(app, text)
    }

    /** 하단 탭에서 이전/다음 섹션으로 한 칸 이동. 이동한 탭 반환 */
    fun moveSection(delta: Int): NavTab {
        val list = _navTabs.value
        val idx = list.indexOfFirst { it.id == _activeSection.value }
        val next = (if (idx < 0) 0 else idx + delta).coerceIn(0, list.size - 1)
        val tab = list[next]
        _activeSection.value = tab.id
        return tab
    }

    fun addCurrentBookmark() {
        val id = _activeSection.value
        val label = _navTabs.value.firstOrNull { it.id == id }?.label ?: id
        viewModelScope.launch {
            repo.addBookmark(BookmarkEntity(sectionId = id, label = label))
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch { repo.deleteBookmark(bookmark) }
    }

    fun addHighlight(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repo.addHighlight(HighlightEntity(text = text))
        }
    }

    fun deleteHighlight(highlight: HighlightEntity) {
        viewModelScope.launch { repo.deleteHighlight(highlight) }
    }
}
