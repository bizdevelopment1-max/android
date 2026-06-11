package com.healthdash.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthdash.app.data.AppDatabase
import com.healthdash.app.data.BookmarkEntity
import com.healthdash.app.data.HighlightEntity
import com.healthdash.app.ui.NAV_TABS
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

    private val _isLandscape = MutableStateFlow(false)
    val isLandscape: StateFlow<Boolean> = _isLandscape.asStateFlow()

    private val _activeSection = MutableStateFlow("overview")
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

    fun toggleLandscape(): Boolean {
        _isLandscape.value = !_isLandscape.value
        return _isLandscape.value
    }

    fun setActiveSection(id: String) {
        if (id.isNotBlank()) _activeSection.value = id
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

    /** AI 앱으로 보낸 텍스트를 히스토리에 기록 */
    fun recordAiSend(app: AiApp, text: String) {
        if (text.isBlank()) return
        _aiHistory.value = _aiHistory.value + AiHistoryItem(app, text)
    }

    /** 하단 탭에서 이전/다음 섹션으로 한 칸 이동. 이동한 탭 id 반환 */
    fun moveSection(delta: Int): String {
        val idx = NAV_TABS.indexOfFirst { it.id == _activeSection.value }
        val next = (if (idx < 0) 0 else idx + delta).coerceIn(0, NAV_TABS.size - 1)
        val tab = NAV_TABS[next]
        _activeSection.value = tab.id
        return tab.id
    }

    fun addCurrentBookmark() {
        val id = _activeSection.value
        val label = NAV_TABS.firstOrNull { it.id == id }?.label ?: id
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
