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
    private val keywordFound: (String) -> Unit,
    private val navExtracted: (String) -> Unit
) {
    @JavascriptInterface
    fun onTextSelected(text: String) = textSelected(text)

    @JavascriptInterface
    fun onSectionVisible(id: String) = sectionVisible(id)

    @JavascriptInterface
    fun onKeywordFound(keywords: String) = keywordFound(keywords)

    /** 사이트 왼쪽 내비에서 추출한 라벨 목록(JSON 배열) */
    @JavascriptInterface
    fun onNavExtracted(json: String) = navExtracted(json)
}

/** WebView 초기화, JS 주입, 핀치 줌 등 WebView 관련 로직 */
object WebViewManager {

    const val DASHBOARD_URL = "https://bizdevelopment1-max.github.io/ai/"
    private const val INTERNAL_HOST = "bizdevelopment1-max.github.io"

    @SuppressLint("SetJavaScriptEnabled")
    fun createDashboardWebView(
        context: Context,
        appSettings: SettingsManager,
        onTextSelected: (String) -> Unit,
        onSectionVisible: (String) -> Unit,
        onKeywordFound: (String) -> Unit,
        onNavExtracted: (String) -> Unit,
        onProgress: (Int) -> Unit,
        onPageFinished: (WebView) -> Unit,
        onMainFrameError: () -> Unit
    ): WebView {
        val wv = WebView(context)
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            textZoom = appSettings.textZoom
            userAgentString = "$userAgentString MXAIInsights/1.0"
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        }
        wv.addJavascriptInterface(
            DashBridge(onTextSelected, onSectionVisible, onKeywordFound, onNavExtracted),
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

            override fun onReceivedError(
                view: WebView?,
                request: android.webkit.WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                if (request?.isForMainFrame == true) onMainFrameError()
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

    /**
     * 탭 → 해당 섹션 이동.
     * HD_NAV가 ①사이트 DASH_NAV → ②id 스크롤 → ③사이트 내비 클릭(라벨 매칭) → ④제목 매칭 스크롤
     * 순으로 폴백 처리한다.
     */
    fun navigateToSection(wv: WebView, sectionId: String, label: String) {
        val idQ = JSONObject.quote(sectionId)
        val labelQ = JSONObject.quote(label)
        // 동적 탭(idx:N)은 추출해 둔 사이트 내비 항목을 직접 클릭
        if (sectionId.startsWith("idx:")) {
            val n = sectionId.removePrefix("idx:").toIntOrNull() ?: -1
            val js = "if (window.HD_NAV_CLICK) { window.HD_NAV_CLICK($n); }" +
                " else if (window.HD_NAV) { window.HD_NAV('', $labelQ); }"
            wv.evaluateJavascript(js, null)
            return
        }
        val js = """
            (function() {
              if (window.HD_NAV) { window.HD_NAV($idQ, $labelQ); return; }
              var el = document.getElementById($idQ);
              if (el) el.scrollIntoView({behavior:'smooth'});
            })();
        """.trimIndent()
        wv.evaluateJavascript(js, null)
    }

    /**
     * 오염된 캐시/서비스워커 저장소로 인한 흰 화면을 방지하기 위해
     * WebView 캐시와 웹 저장소를 완전히 비운다.
     */
    fun clearWebStorage(wv: WebView) {
        try {
            wv.clearCache(true)
            wv.clearHistory()
            android.webkit.WebStorage.getInstance().deleteAllData()
        } catch (_: Exception) {
        }
    }

    /** 화면 높이의 fraction 배만큼 부드럽게 스크롤 (윈도우/내부 컨테이너 자동 감지) */
    fun scrollPage(wv: WebView, fraction: Double) {
        wv.evaluateJavascript("if (window.HD_SCROLL) window.HD_SCROLL($fraction);", null)
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

          function hdReady(fn) {
            if (document.readyState === 'loading') {
              document.addEventListener('DOMContentLoaded', fn);
            } else {
              fn();
            }
          }

          // 텍스트 선택 감지 → 네이티브 AI 바 표시
          document.addEventListener('selectionchange', function() {
            try {
              var sel = window.getSelection();
              var text = sel ? sel.toString().trim() : '';
              AndroidBridge.onTextSelected(text || '');
            } catch (e) {}
          });

          // 네이티브 탭에서 섹션 이동 — 어떤 페이지 구조에서도 동작하도록 다단계 폴백
          window.HD_NAV = function(id, label) {
            function norm(s) { return (s || '').replace(/\s+/g, '').toLowerCase(); }
            function scrollToEl(el) {
              try { el.scrollIntoView({ behavior: 'smooth', block: 'start' }); return true; } catch (e) { return false; }
            }
            try {
              // 1) 사이트가 자체 제공하는 내비 훅
              if (typeof window.DASH_NAV === 'function') {
                try { window.DASH_NAV(id); } catch (e) {}
              }
              // 2) 섹션 id로 직접 스크롤
              var el = document.getElementById(id);
              if (el) return scrollToEl(el);
              el = document.querySelector('[data-section="' + id + '"]');
              if (el) return scrollToEl(el);
              var target = norm(label);
              if (!target) return false;
              // 3) 사이트 자체 내비게이션(탭/메뉴)에서 같은 라벨을 찾아 클릭 (SPA 탭 전환 대응)
              var navs = document.querySelectorAll('a, button, [role="tab"], [role="button"], li, nav span, [class*="tab"], [class*="nav"], [class*="menu"]');
              for (var i = 0; i < navs.length; i++) {
                var t = norm(navs[i].textContent);
                if (!t || t.length > 24) continue;
                if (t === target || t.indexOf(target) >= 0 || (target.indexOf(t) >= 0 && t.length >= 2)) {
                  try { navs[i].click(); return true; } catch (e) {}
                }
              }
              // 4) 본문 제목 텍스트 매칭으로 스크롤
              var heads = document.querySelectorAll('h1, h2, h3, h4, [class*="title"], [class*="header"], [class*="head"]');
              for (var j = 0; j < heads.length; j++) {
                var ht = norm(heads[j].textContent);
                if (!ht || ht.length > 40) continue;
                if (ht.indexOf(target) >= 0 || (target.indexOf(ht) >= 0 && ht.length >= 2)) {
                  return scrollToEl(heads[j]);
                }
              }
            } catch (e) {}
            return false;
          };

          // 검색 지원
          window.DASH_SEARCH = window.DASH_SEARCH || function(query) {
            window.dispatchEvent(new CustomEvent('nativeSearch', { detail: { query: query } }));
          };

          // 사이트 왼쪽 내비게이션을 추출해 하단 탭과 일치시키기
          window.HD_EXTRACT_NAV = function() {
            function txt(el) { return (el.textContent || '').replace(/\s+/g, ' ').trim(); }
            var sel = 'nav, aside, [role="navigation"], [role="tablist"], [role="menu"],' +
              ' [class*="sidebar"], [class*="side-nav"], [class*="sidenav"], [class*="side_bar"],' +
              ' [class*="menu"], [class*="nav"], [class*="tabs"], [id*="sidebar"], [id*="nav"]';
            var containers = Array.prototype.slice.call(document.querySelectorAll(sel));
            var best = null, bestScore = -1;
            containers.forEach(function(c) {
              var raw = Array.prototype.slice.call(
                c.querySelectorAll('a, button, li, [role="tab"], [role="menuitem"], [role="button"]'));
              var seen = {}, list = [];
              raw.forEach(function(it) {
                var t = txt(it);
                if (!t || t.length > 28) return;
                if (it.querySelector('a, button, [role="tab"], [role="menuitem"]')) return;
                if (seen[t]) return;
                seen[t] = 1;
                list.push(it);
              });
              if (list.length < 3 || list.length > 24) return;
              var r = c.getBoundingClientRect();
              var score = list.length;
              if (r.left < window.innerWidth * 0.45) score += 6;   // 왼쪽 배치 가산
              if (r.height >= r.width) score += 3;                  // 세로 배치 가산
              if (score > bestScore) { bestScore = score; best = list; }
            });
            if (!best) return null;
            window.__HD_NAV__ = best;
            return best.map(txt);
          };

          window.HD_NAV_CLICK = function(i) {
            try {
              var el = window.__HD_NAV__ && window.__HD_NAV__[i];
              if (el) {
                el.click();
                el.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                return true;
              }
            } catch (e) {}
            return false;
          };

          function hdReportNav() {
            try {
              var labels = window.HD_EXTRACT_NAV();
              if (labels && labels.length >= 3 && window.AndroidBridge && AndroidBridge.onNavExtracted) {
                AndroidBridge.onNavExtracted(JSON.stringify(labels));
              }
            } catch (e) {}
          }
          hdReady(function() { hdReportNav(); });
          setTimeout(hdReportNav, 1200);
          setTimeout(hdReportNav, 2800);

          // 플로팅 버튼 스크롤 — 윈도우 또는 가장 큰 내부 스크롤 컨테이너를 자동 감지
          window.HD_SCROLL = function(frac) {
            try {
              var dy = Math.round(window.innerHeight * frac);
              var doc = document.scrollingElement || document.documentElement;
              if (doc && doc.scrollHeight > window.innerHeight + 10) {
                window.scrollBy({ top: dy, behavior: 'smooth' });
                return true;
              }
              var el = window.__HD_SCROLL_EL__;
              if (!el || !document.body.contains(el)) {
                el = null;
                var els = document.querySelectorAll('div, main, section');
                for (var i = 0; i < els.length; i++) {
                  var c = els[i];
                  if (c.scrollHeight > c.clientHeight + 50 && c.clientHeight > window.innerHeight * 0.4) {
                    var st = getComputedStyle(c);
                    if (st.overflowY === 'auto' || st.overflowY === 'scroll' || st.overflow === 'auto') {
                      if (!el || c.clientHeight > el.clientHeight) el = c;
                    }
                  }
                }
                window.__HD_SCROLL_EL__ = el;
              }
              if (el) { el.scrollBy({ top: dy, behavior: 'smooth' }); return true; }
            } catch (e) {}
            return false;
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
          hdReady(function() {
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
          });
        })();
    """.trimIndent()
}
