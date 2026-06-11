package com.healthdash.app

import android.content.Context
import android.content.SharedPreferences

/** SharedPreferences 래퍼 — 텍스트 줌, 스플릿 비율 등 앱 상태 영속화 */
class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("healthdash_prefs", Context.MODE_PRIVATE)

    var textZoom: Int
        get() = prefs.getInt(KEY_TEXT_ZOOM, 100)
        set(value) = prefs.edit().putInt(KEY_TEXT_ZOOM, value.coerceIn(MIN_ZOOM, MAX_ZOOM)).apply()

    var highContrast: Boolean
        get() = prefs.getBoolean(KEY_HIGH_CONTRAST, false)
        set(value) = prefs.edit().putBoolean(KEY_HIGH_CONTRAST, value).apply()

    var ttsSpeed: Float
        get() = prefs.getFloat(KEY_TTS_SPEED, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_TTS_SPEED, value.coerceIn(0.5f, 2.0f)).apply()

    var keywords: String
        get() = prefs.getString(KEY_KEYWORDS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_KEYWORDS, value).apply()

    companion object {
        const val MIN_ZOOM = 70
        const val MAX_ZOOM = 200
        private const val KEY_TEXT_ZOOM = "textZoom"
        private const val KEY_HIGH_CONTRAST = "highContrast"
        private const val KEY_TTS_SPEED = "ttsSpeed"
        private const val KEY_KEYWORDS = "keywords"
    }
}
