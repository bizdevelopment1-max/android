package com.healthdash.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ScaleGestureDetector
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import org.json.JSONObject

/** 웹 → 네이티브 콜백용 JavaScript Bridge (`window.AndroidBridge`) */
class DashBridge(
    private val textSelected: (String) -> Unit,
    private val sectionVisible: (String) -> Unit,
    private val keywordFound: (String) -> Unit
) {
    @JavascriptInterface
    fun onTextSelected(text: String) = textSelected(text)

    @JavascriptInterface
    fun onSectionVisible(id: String) = sectionVisible(id)

    @JavascriptInterface
    fun onKeywordFound(keywords: String) = keywordFound(keywords)
}

/** WebView 초기화, JS 주입, 핀치 줌 등 WebView 관련 로직 */
object WebViewManager {

    const val DASHBOARD_URL = "https://bizdevelopment1-max.github.io/health/"
    private const val INTERNAL_HOST = "bizdevelopment1-max.github.io"

    @SuppressLint("SetJavaScriptEnabled")
    fun createDashboardWebView(
        context: Context,
        appSettings: SettingsManager,
        onTextSelected: (String) -> Unit,
        onSectionVisible: (String) -> Unit,
        onKeywordFound: (String) -> Unit,
        onProgress: (Int) -> Unit,
        onPageFinished: (WebView) -> Unit
    ): WebView {
        val wv = WebView(context)
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            textZoom = appSettings.textZoom
            userAgentString = "$userAgentString HealthDashApp/1.0"
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        }
        wv.addJavascriptInterface(
            DashBridge(onTextSelected, onSectionVisible, onKeywordFound),
            "AndroidBridge"
        )
        wv.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                onProgress(newProgress)
            }
        }
        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                if (uri.host?.contains(INTERNAL_HOST) == true) return false
                openExternal(context, uri)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                view?.let(onPageFinished)
            }
        }
        return wv
    }

    /** 외부 URL은 Custom Tabs로 (실패 시 일반 브라우저) */
    private fun openExternal(context: Context, uri: Uri) {
        try {
            CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, uri)
        } catch (e: Exception) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (_: Exception) {
            }
        }
    }

    fun injectInitScript(wv: WebView) {
        wv.evaluateJavascript(INIT_SCRIPT, null)
    }

    /** 탭 → 해당 섹션 스크롤 (DASH_NAV 없으면 scrollIntoView 폴백) */
    fun navigateToSection(wv: WebView, sectionId: String) {
        val js = "if (window.DASH_NAV) { window.DASH_NAV('" + sectionId + "'); } " +
            "else { var el = document.getElementById('" + sectionId + "'); " +
            "if (el) el.scrollIntoView({behavior:'smooth'}); }"
        wv.evaluateJavascript(js, null)
    }

    fun search(wv: WebView, query: String) {
        val quoted = JSONObject.quote(query)
        wv.evaluateJavascript("if (window.DASH_SEARCH) window.DASH_SEARCH($quoted);", null)
        if (query.isBlank()) wv.clearMatches() else wv.findAllAsync(query)
    }

    /** 시스템 다크모드 상태를 웹 앱에 전달 */
    fun injectTheme(wv: WebView, dark: Boolean) {
        wv.evaluateJavascript(
            "window.dispatchEvent(new CustomEvent('nativeTheme', {detail:{dark: $dark}}));",
            null
        )
    }

    /** 고대비 모드 CSS 주입/제거 */
    fun injectHighContrast(wv: WebView, enabled: Boolean) {
        val js = """
            (function() {
              var id = 'hd-high-contrast';
              var el = document.getElementById(id);
              if ($enabled) {
                if (!el) {
                  el = document.createElement('style');
                  el.id = id;
                  el.textContent = '* { background-color: #ffffff !important; color: #000000 !important; border-color: #555555 !important; } a { color: #0030c0 !important; }';
                  document.head.appendChild(el);
                }
              } else if (el) {
                el.remove();
              }
            })();
        """.trimIndent()
        wv.evaluateJavascript(js, null)
    }

    /** 관심 키워드가 페이지에 있으면 네이티브로 알림 */
    fun injectKeywordCheck(wv: WebView, keywords: String) {
        if (keywords.isBlank()) return
        val quoted = JSONObject.quote(keywords)
        val js = """
            (function() {
              try {
                var found = [];
                var body = document.body ? document.body.innerText : '';
                $quoted.split(',').forEach(function(k) {
                  k = k.trim();
                  if (k && body.indexOf(k) >= 0) found.push(k);
                });
                if (found.length > 0 && window.AndroidBridge) AndroidBridge.onKeywordFound(found.join(', '));
              } catch (e) {}
            })();
        """.trimIndent()
        wv.evaluateJavascript(js, null)
    }

    /** 저장된 하이라이트 텍스트를 노란 형광펜으로 표시 */
    fun injectHighlight(wv: WebView, text: String) {
        if (text.isBlank()) return
        val quoted = JSONObject.quote(text)
        wv.evaluateJavascript("if (window.HD_HIGHLIGHT) window.HD_HIGHLIGHT($quoted);", null)
    }

    /** 핀치 줌 제스처로 textZoom 직접 제어 */
    @SuppressLint("ClickableViewAccessibility")
    fun attachPinchZoom(wv: WebView, getZoom: () -> Int, setZoom: (Int) -> Unit) {
        val detector = ScaleGestureDetector(
            wv.context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val f = detector.scaleFactor
                    if (f > 1.05f) {
                        setZoom((getZoom() + 2).coerceAtMost(SettingsManager.MAX_ZOOM))
                    } else if (f < 0.95f) {
                        setZoom((getZoom() - 2).coerceAtLeast(SettingsManager.MIN_ZOOM))
                    }
                    return true
                }
            }
        )
        wv.setOnTouchListener { _, event ->
            detector.onTouchEvent(event)
            false
        }
    }

    private val INIT_SCRIPT = """
        (function() {
          if (window.__HD_INIT__) return;
          window.__HD_INIT__ = true;

          // 텍스트 선택 감지 → 네이티브 AI 바 표시
          document.addEventListener('selectionchange', function() {
            try {
              var sel = window.getSelection();
              var text = sel ? sel.toString().trim() : '';
              AndroidBridge.onTextSelected(text || '');
            } catch (e) {}
          });

          // 네이티브 탭에서 섹션 이동 지원
          window.DASH_NAV = window.DASH_NAV || function(sectionId) {
            var mapping = {
              overview: 'overview', device: 'device', ai: 'ai', startup: 'startup',
              vp: 'vp', articles: 'articles', charts: 'charts', monthly: 'monthly',
              insights: 'insights', dynamics: 'dynamics', bizmodel: 'bizmodel', reports: 'reports'
            };
            var el = document.getElementById(mapping[sectionId] || sectionId);
            var mainScroll = document.querySelector('.main-scroll, main, #root > div > div:last-child');
            if (el && mainScroll && mainScroll.scrollHeight > mainScroll.clientHeight) {
              mainScroll.scrollTo({ top: el.offsetTop - 12, behavior: 'smooth' });
            } else if (el) {
              el.scrollIntoView({ behavior: 'smooth' });
            }
          };

          // 검색 지원
          window.DASH_SEARCH = window.DASH_SEARCH || function(query) {
            window.dispatchEvent(new CustomEvent('nativeSearch', { detail: { query: query } }));
          };

          // 하이라이트 (첫 번째 일치 텍스트를 mark로 감쌈)
          window.HD_HIGHLIGHT = function(text) {
            try {
              if (!text) return false;
              var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null);
              var node;
              while ((node = walker.nextNode())) {
                if (node.parentElement && node.parentElement.tagName === 'MARK') continue;
                var idx = node.nodeValue.indexOf(text);
                if (idx >= 0) {
                  var range = document.createRange();
                  range.setStart(node, idx);
                  range.setEnd(node, idx + text.length);
                  var mark = document.createElement('mark');
                  mark.style.backgroundColor = '#FFF59D';
                  try { range.surroundContents(mark); } catch (e) { return false; }
                  return true;
                }
              }
            } catch (e) {}
            return false;
          };

          // 스크롤 스파이 → 현재 섹션을 네이티브로 전달
          try {
            var observer = new IntersectionObserver(function(entries) {
              entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                  try { AndroidBridge.onSectionVisible(entry.target.id || ''); } catch (e) {}
                }
              });
            }, { threshold: 0.3 });
            document.querySelectorAll('section[id], section.board, [id]').forEach(function(el) {
              if (el.id && el.tagName === 'SECTION') observer.observe(el);
            });
          } catch (e) {}
        })();
    """.trimIndent()
}
