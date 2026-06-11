package com.healthdash.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/** 텍스트 선택 시 보여줄 4개 AI 앱 정의 */
enum class AiApp(
    val displayName: String,
    val color: Long,
    val packageName: String,
    val iconRes: Int,
    private val queryUrl: String
) {
    CHATGPT("ChatGPT", 0xFF10A37F, "com.openai.chatgpt", R.drawable.ic_ai_chatgpt, "https://chatgpt.com/?q="),
    GEMINI("Gemini", 0xFF4285F4, "com.google.android.apps.bard", R.drawable.ic_ai_gemini, "https://gemini.google.com/app?q="),
    CLAUDE("Claude", 0xFFD97757, "com.anthropic.claude", R.drawable.ic_ai_claude, "https://claude.ai/new?q="),
    PERPLEXITY("Perplexity", 0xFF20808D, "ai.perplexity.app.android", R.drawable.ic_ai_perplexity, "https://www.perplexity.ai/search?q=");

    fun urlFor(text: String): String =
        if (text.isBlank()) queryUrl.substringBefore("?")
        else queryUrl + URLEncoder.encode(text.take(2000), "UTF-8")
}

/** 클립보드 복사, 외부 AI 앱 실행, 번역 등 텍스트 선택 액션 처리 */
object AiBarManager {

    fun copyToClipboard(context: Context, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("HealthDash", text))
    }

    fun isInstalled(context: Context, packageName: String): Boolean = try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: Exception) {
        false
    }

    /**
     * 로그인된 네이티브 AI 앱을 분할 화면(LAUNCH_ADJACENT)으로 실행하고 텍스트를 전달.
     * 시도 순서:
     *  1) ACTION_SEND 공유 인텐트 — 텍스트가 AI 앱 입력으로 바로 들어감
     *  2) 앱 딥링크 (https://…?q=텍스트) — 앱이 URL을 처리하면 질문이 채워짐
     *  3) 앱 단순 실행 — 클립보드에서 붙여넣기
     * 앱 미설치 시 웹 URL을 외부 브라우저로 연다. 텍스트는 항상 클립보드에 복사된다.
     */
    fun launchExternalApp(context: Context, app: AiApp, text: String) {
        if (text.isNotBlank()) copyToClipboard(context, text)
        val adjacentFlags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
        if (isInstalled(context, app.packageName)) {
            if (text.isNotBlank()) {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    setPackage(app.packageName)
                    addFlags(adjacentFlags)
                }
                try {
                    context.startActivity(send)
                    return
                } catch (_: Exception) {
                }
                val deepLink = Intent(Intent.ACTION_VIEW, Uri.parse(app.urlFor(text))).apply {
                    setPackage(app.packageName)
                    addFlags(adjacentFlags)
                }
                try {
                    context.startActivity(deepLink)
                    return
                } catch (_: Exception) {
                }
            }
            val launch = context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (launch != null) {
                try {
                    launch.addFlags(adjacentFlags)
                    context.startActivity(launch)
                    Toast.makeText(context, "클립보드의 텍스트를 붙여넣어 주세요", Toast.LENGTH_SHORT).show()
                    return
                } catch (_: Exception) {
                }
            }
        }
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(app.urlFor(text))).addFlags(adjacentFlags))
            Toast.makeText(context, "${app.displayName} 앱이 없어 브라우저로 엽니다", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
        }
    }

    /** 한국어↔영어 자동 번역 (Google Translate 비공식 엔드포인트, 실패 시 null) */
    suspend fun translate(text: String): String? = withContext(Dispatchers.IO) {
        try {
            val hasKorean = text.any { it in '가'..'힣' }
            val target = if (hasKorean) "en" else "ko"
            val q = URLEncoder.encode(text.take(1500), "UTF-8")
            val url = URL("https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=$target&dt=t&q=$q")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            val rows = JSONArray(body).getJSONArray(0)
            buildString {
                for (i in 0 until rows.length()) append(rows.getJSONArray(i).getString(0))
            }.ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }
}

/** 선택 텍스트 TTS 읽기 (속도 0.5x ~ 2.0x) */
class TtsManager(context: Context) {
    private var ready = false
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.KOREAN
                ready = true
            }
        }
    }

    fun speak(text: String, speed: Float) {
        if (!ready || text.isBlank()) return
        tts?.setSpeechRate(speed.coerceIn(0.5f, 2.0f))
        tts?.speak(text.take(3900), TextToSpeech.QUEUE_FLUSH, null, "healthdash-tts")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
