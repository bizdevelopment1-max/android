package com.healthdash.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import kotlin.math.abs
import kotlin.random.Random

// ── 디자인 데이터 (MX AI Insights.dc.html 포팅) ──────────────────────────────
data class MxTheme(val name: String, val g1: Color, val g2: Color, val glow: Color)

val MX_THEMES = listOf(
    MxTheme("Violet", Color(0xFF6D28D9), Color(0xFF2563EB), Color(0xFF22D3EE)),
    MxTheme("Magenta", Color(0xFF7C3AED), Color(0xFFC026D3), Color(0xFFF0ABFC)),
    MxTheme("Teal", Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF67E8F9)),
    MxTheme("Sunset", Color(0xFFF97316), Color(0xFFDB2777), Color(0xFFFDBA74)),
    MxTheme("Lime", Color(0xFF16A34A), Color(0xFF06B6D4), Color(0xFF86EFAC)),
    MxTheme("Slate", Color(0xFF64748B), Color(0xFF1E293B), Color(0xFF94A3B8))
)

private data class MxCat(val id: String, val tab: String, val eyebrow: String, val title: String, val sub: String)

private val MX_CATS = listOf(
    MxCat("market", "Market", "COMPETITIVE DYNAMICS", "Who is winning the AI race",
        "Live language-model market share, refreshed every minute."),
    MxCat("models", "Models", "MODEL ARENA", "Plug in any model",
        "Switch between the world's leading AI engines — your call."),
    MxCat("pricing", "Pricing", "MONETIZATION", "How it pays for itself",
        "Free to start. Upgrade when you walk further."),
    MxCat("dynamics", "Dynamics", "ADAPTIVE ENGINE", "It learns as you walk",
        "Retrains on every word you meet — tuned to your level and pace."),
    MxCat("trends", "Trends", "TODAY’S SIGNALS", "What the data tells you",
        "The signals our AI flagged for you in the last 24 hours.")
)

private data class Vendor(val k: String, val name: String, val sub: String, val share: Int, val delta: Double)

private val MX_VENDORS = listOf(
    Vendor("openai", "OpenAI", "GPT-4o", 28, 2.1),
    Vendor("claude", "Claude", "Anthropic", 23, 3.4),
    Vendor("gemini", "Gemini", "Google", 18, 1.2),
    Vendor("meta", "Llama", "Meta", 13, -0.6),
    Vendor("mistral", "Mistral", "Open", 8, 0.9),
    Vendor("perplexity", "Perplexity", "Search", 6, 1.7),
    Vendor("grok", "Grok", "xAI", 4, 0.4)
)

private data class Plan(val id: String, val name: String, val price: String, val per: String?, val line: String, val tag: String?)

private val MX_PLANS = listOf(
    Plan("free", "Free", "₩0", null, "Daily 30 lookups", null),
    Plan("pro", "Pro", "₩4,900", "/mo", "Unlimited · offline · no ads", "POPULAR"),
    Plan("life", "Lifetime", "₩49,000", null, "Pay once · forever", null)
)

private data class Trend(val g: String, val s: String, val v: String, val up: Boolean, val spark: List<Int>)

private val MX_TRENDS = listOf(
    Trend("Korean ↔ English", "Most-asked pair today", "+18%", true, listOf(3, 5, 4, 7, 6, 9, 8)),
    Trend("Menu & food terms", "Trending context", "+42%", true, listOf(2, 3, 3, 5, 6, 7, 9)),
    Trend("Street signage", "Camera lookups", "-5%", false, listOf(8, 7, 7, 6, 5, 6, 5)),
    Trend("Idiom requests", "New this week", "+11%", true, listOf(4, 4, 5, 5, 6, 6, 7))
)

private val Mono = FontFamily.Monospace
private val BASE = Color(0xFF08060F)
private val TXT = Color(0xFFF5F3FF)
private val SUB = Color(0xFFB8B3D4)
private val MUTED = Color(0xFF7E84AE)
private val HAIR = Color(0x1AFFFFFF)
private val CHIP = Color(0x0FFFFFFF)
private val CARD = Color(0x0DFFFFFF)
private val CARDSEL = Color(0x1AFFFFFF)
private val POS = Color(0xFF34D399)
private val NEG = Color(0xFFF87171)

/**
 * 디자인 런치 페이지 — 다크 배경 + 떠다니는 오브, 6개 테마 스와치, 5개 인사이트 탭.
 * 테마/탭에 따라 색상·콘텐츠가 바뀌고, Start 버튼으로 대시보드로 진입한다.
 */
@Composable
fun LaunchScreen(
    themeIndex: Int,
    onSelectTheme: (Int) -> Unit,
    onStart: () -> Unit
) {
    val th = MX_THEMES[themeIndex.coerceIn(0, MX_THEMES.size - 1)]
    val grad = Brush.linearGradient(listOf(th.g1, th.g2))
    var cat by remember { mutableStateOf("market") }
    var sig by remember { mutableIntStateOf(1840275) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(110)
            sig += Random.nextInt(5, 35)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(BASE)
    ) {
        AnimatedOrbs(th)
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            TopBar(th, grad, themeIndex, onSelectTheme)
            CatChips(th, grad, cat) { cat = it }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Hero(th, grad, cat, sig)
                Box(Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                    Panel(th, grad, cat)
                }
                VendorStrip()
                Spacer(Modifier.height(8.dp))
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0x8C08060F))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                StartButton(grad, onStart)
                Spacer(Modifier.height(11.dp))
                Footer()
            }
        }
    }
}

@Composable
private fun AnimatedOrbs(th: MxTheme) {
    val tr = rememberInfiniteTransition(label = "orbs")
    val a by tr.animateFloat(0f, 1f, infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse), label = "a")
    val b by tr.animateFloat(0f, 1f, infiniteRepeatable(tween(13000, easing = LinearEasing), RepeatMode.Reverse), label = "b")
    Box(Modifier.fillMaxSize()) {
        Orb(th.g1, 300.dp, (-70).dp + (24 * a).dp, (-90).dp + (28 * a).dp, 0.55f)
        Orb(th.g2, 280.dp, (220 - 28 * b).dp, (520 - 22 * b).dp, 0.55f)
        Orb(th.glow, 200.dp, (120 + 24 * a).dp, (360 - 20 * a).dp, 0.28f)
        TwinkleField(th)
    }
}

@Composable
private fun Orb(color: Color, size: androidx.compose.ui.unit.Dp, x: androidx.compose.ui.unit.Dp, y: androidx.compose.ui.unit.Dp, alpha: Float) {
    Box(
        Modifier
            .offset(x = x, y = y)
            .size(size)
            .background(
                Brush.radialGradient(listOf(color.copy(alpha = alpha), Color.Transparent)),
                CircleShape
            )
    )
}

@Composable
private fun TwinkleField(th: MxTheme) {
    val tr = rememberInfiniteTransition(label = "tw")
    val a by tr.animateFloat(0.25f, 1f, infiniteRepeatable(tween(2600), RepeatMode.Reverse), label = "tw")
    Box(Modifier.fillMaxSize()) {
        for (i in 0 until 9) {
            val color = if (i % 2 == 0) th.g1 else th.glow
            Box(
                Modifier
                    .offset(x = ((i * 11 + 7) % 100 * 3.4).dp, y = ((i * 23 + 9) % 100 * 6.0).dp)
                    .size((2 + i % 3).dp)
                    .background(color.copy(alpha = if (i % 2 == 0) a else 1f - a + 0.25f), CircleShape)
            )
        }
    }
}

@Composable
private fun TopBar(th: MxTheme, grad: Brush, themeIndex: Int, onSelectTheme: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(grad),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("MX", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = (-1).sp, lineHeight = 12.sp)
                Text("AI Insights", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 4.5.sp)
            }
        }
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text("AI Insights", color = TXT, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 14.sp)
            Text("BY MX", color = MUTED, fontFamily = Mono, fontSize = 8.sp, letterSpacing = 1.5.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            MX_THEMES.forEachIndexed { i, t ->
                val on = themeIndex == i
                Box(
                    Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(t.g1, t.g2)))
                        .then(if (on) Modifier.border(2.dp, t.g1, CircleShape) else Modifier)
                        .clickable { onSelectTheme(i) }
                )
            }
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(HAIR))
}

@Composable
private fun CatChips(th: MxTheme, grad: Brush, cat: String, onCat: (String) -> Unit) {
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        MX_CATS.forEach { c ->
            val on = cat == c.id
            Box(
                Modifier
                    .clip(RoundedCornerShape(100))
                    .then(if (on) Modifier.background(grad) else Modifier.background(CHIP).border(1.dp, HAIR, RoundedCornerShape(100)))
                    .clickable { onCat(c.id) }
                    .padding(horizontal = 13.dp, vertical = 7.dp)
            ) {
                Text(c.tab, color = if (on) Color.White else SUB, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Hero(th: MxTheme, grad: Brush, cat: String, sig: Int) {
    val c = MX_CATS.first { it.id == cat }
    val tr = rememberInfiniteTransition(label = "dot")
    val p by tr.animateFloat(0.3f, 1f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "p")
    Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).background(th.glow.copy(alpha = p), CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(c.eyebrow, color = MUTED, fontFamily = Mono, fontSize = 10.sp, letterSpacing = 2.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(c.title, color = TXT, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 27.sp, letterSpacing = (-0.7).sp)
        Spacer(Modifier.height(7.dp))
        Text(c.sub, color = SUB, fontSize = 12.5.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(9.dp))
        Row {
            Text(formatThousands(sig), fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = th.g2, letterSpacing = 1.sp)
            Text(" INSIGHTS GENERATED", color = MUTED, fontFamily = Mono, fontSize = 10.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun Panel(th: MxTheme, grad: Brush, cat: String) {
    when (cat) {
        "market" -> MarketPanel(th, grad)
        "models" -> ModelsPanel(th, grad)
        "pricing" -> PricingPanel(th, grad)
        "dynamics" -> DynamicsPanel(th, grad)
        else -> TrendsPanel(th)
    }
}

@Composable
private fun cardBox(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CARD)
            .border(1.dp, HAIR, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) { content() }
}

@Composable
private fun MarketPanel(th: MxTheme, grad: Brush) {
    val max = MX_VENDORS.maxOf { it.share }
    cardBox {
        Column {
            MX_VENDORS.forEachIndexed { i, v ->
                if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(HAIR))
                Row(
                    Modifier.padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${i + 1}", color = MUTED, fontFamily = Mono, fontSize = 10.sp, modifier = Modifier.width(14.dp))
                    Spacer(Modifier.width(11.dp))
                    VendorBadge(v.k, Color(0xFFE7E5F4))
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(v.name, color = TXT, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            Text("${v.share}%", color = TXT, fontFamily = Mono, fontSize = 11.sp)
                        }
                        Spacer(Modifier.height(5.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x12FFFFFF))
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(v.share.toFloat() / max)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(grad)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        (if (v.delta >= 0) "▲" else "▼") + abs(v.delta),
                        color = if (v.delta >= 0) POS else NEG,
                        fontFamily = Mono, fontSize = 10.sp,
                        modifier = Modifier.width(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelsPanel(th: MxTheme, grad: Brush) {
    var model by remember { mutableStateOf("openai") }
    cardBox {
        Column {
            val rows = MX_VENDORS.take(6).chunked(2)
            rows.forEach { pair ->
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { v ->
                        val on = model == v.k
                        Row(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(13.dp))
                                .then(if (on) Modifier.background(CARDSEL).border(1.5.dp, th.g1, RoundedCornerShape(13.dp)) else Modifier.border(1.dp, HAIR, RoundedCornerShape(13.dp)))
                                .clickable { model = v.k }
                                .padding(horizontal = 11.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(if (on) grad else Color(0x0FFFFFFF)),
                                contentAlignment = Alignment.Center
                            ) { VendorGlyphRaw(v.k, if (on) Color.White else Color(0xFFE7E5F4), 17.dp) }
                            Spacer(Modifier.width(9.dp))
                            Column {
                                Text(v.name, color = TXT, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 12.sp)
                                Text(v.sub, color = MUTED, fontFamily = Mono, fontSize = 8.5.sp)
                            }
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                "One subscription, every model. Routed automatically to the best engine for each lookup.",
                color = SUB, fontSize = 11.5.sp, lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun PricingPanel(th: MxTheme, grad: Brush) {
    var plan by remember { mutableStateOf("pro") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MX_PLANS.forEach { p ->
            val sel = plan == p.id
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .then(if (sel) Modifier.background(CARDSEL).border(1.5.dp, th.g1, RoundedCornerShape(14.dp)) else Modifier.background(CARD).border(1.dp, HAIR, RoundedCornerShape(14.dp)))
                    .clickable { plan = p.id }
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(17.dp).clip(CircleShape).border(2.dp, if (sel) th.g1 else MUTED, CircleShape)
                        .then(if (sel) Modifier.background(grad) else Modifier),
                    contentAlignment = Alignment.Center
                ) { if (sel) Box(Modifier.size(5.dp).background(Color.White, CircleShape)) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(p.name, color = TXT, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    Text(p.line, color = SUB, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(p.price, color = TXT, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (p.per != null) Text(p.per, color = SUB, fontFamily = Mono, fontSize = 9.sp)
                }
                if (p.tag != null) {
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.clip(RoundedCornerShape(100)).background(grad).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(p.tag, color = Color.White, fontFamily = Mono, fontSize = 8.sp, letterSpacing = 0.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicsPanel(th: MxTheme, grad: Brush) {
    val heights = listOf(0.5f, 0.8f, 0.4f, 1f, 0.65f, 0.9f, 0.55f, 0.75f, 0.6f, 0.85f)
    val tr = rememberInfiniteTransition(label = "bars")
    cardBox {
        Column {
            Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                heights.forEachIndexed { i, base ->
                    val f by tr.animateFloat(
                        base * 0.45f, base,
                        infiniteRepeatable(tween(1400 + i * 100), RepeatMode.Reverse), label = "b$i"
                    )
                    Box(
                        Modifier.weight(1f).fillMaxHeight(f).clip(RoundedCornerShape(4.dp)).background(grad)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("98.4%" to "ACCURACY", "40ms" to "LATENCY", "12.8k" to "WORDS LEARNED").forEach { (v, k) ->
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x0AFFFFFF))
                            .border(1.dp, HAIR, RoundedCornerShape(12.dp))
                            .padding(vertical = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(v, color = TXT, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(3.dp))
                        Text(k, color = MUTED, fontFamily = Mono, fontSize = 8.sp, letterSpacing = 0.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendsPanel(th: MxTheme) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MX_TRENDS.forEach { tr ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CARD)
                    .border(1.dp, HAIR, RoundedCornerShape(14.dp))
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(tr.g, color = TXT, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(tr.s, color = SUB, fontSize = 10.5.sp)
                }
                Spacer(Modifier.width(11.dp))
                Sparkline(tr.spark, if (tr.up) th.glow else NEG)
                Spacer(Modifier.width(11.dp))
                Text(tr.v, color = if (tr.up) POS else NEG, fontFamily = Mono, fontSize = 11.sp, modifier = Modifier.width(42.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
        }
    }
}

@Composable
private fun Sparkline(points: List<Int>, color: Color) {
    val mx = points.max()
    androidx.compose.foundation.Canvas(Modifier.size(56.dp, 22.dp)) {
        val stepX = size.width / (points.size - 1)
        val path = androidx.compose.ui.graphics.Path()
        points.forEachIndexed { i, y ->
            val px = i * stepX
            val py = size.height - (y.toFloat() / mx) * (size.height * 0.85f)
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun VendorStrip() {
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("WORKS WITH THE WORLD’S LEADING AI", color = MUTED, fontFamily = Mono, fontSize = 9.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MX_VENDORS.forEach { v ->
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color(0x0DFFFFFF)).border(1.dp, HAIR, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) { VendorGlyphRaw(v.k, Color(0xFFCBC9DE), 18.dp) }
            }
        }
    }
}

@Composable
private fun StartButton(grad: Brush, onStart: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(grad)
            .clickable { onStart() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Start", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            androidx.compose.foundation.Canvas(Modifier.size(20.dp)) {
                val s = size.minDimension
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(s * 0.21f, s * 0.5f); lineTo(s * 0.79f, s * 0.5f)
                    moveTo(s * 0.54f, s * 0.25f); lineTo(s * 0.79f, s * 0.5f); lineTo(s * 0.54f, s * 0.75f)
                }
                drawPath(p, Color.White, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
            }
        }
    }
}

@Composable
private fun Footer() {
    Text(
        "MADE BY BD GROUP",
        color = MUTED, fontFamily = Mono, fontSize = 10.sp, letterSpacing = 2.sp,
        modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@Composable
private fun VendorBadge(k: String, color: Color) {
    Box(
        Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(CARDSEL).border(1.dp, HAIR, RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center
    ) { VendorGlyphRaw(k, color, 17.dp) }
}

/** 각 AI 벤더 글리프 (디자인 vGlyph 포팅, Canvas) */
@Composable
private fun VendorGlyphRaw(k: String, color: Color, sz: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.Canvas(Modifier.size(sz)) {
        val s = size.minDimension
        val w = 1.8f / 24f * s
        fun u(v: Float) = v / 24f * s
        when (k) {
            "claude" -> {
                for (i in 0 until 8) {
                    val a = Math.toRadians((i * 45).toDouble())
                    val r1 = u(3f); val r2 = if (i % 2 == 1) u(8.5f) else u(7f)
                    drawLine(color,
                        Offset((u(12f) + r1 * Math.cos(a)).toFloat(), (u(12f) + r1 * Math.sin(a)).toFloat()),
                        Offset((u(12f) + r2 * Math.cos(a)).toFloat(), (u(12f) + r2 * Math.sin(a)).toFloat()),
                        strokeWidth = 2f / 24f * s, cap = StrokeCap.Round)
                }
            }
            "gemini" -> {
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(u(12f), u(2.5f)); cubicTo(u(12f), u(7.5f), u(12.5f), u(9.5f), u(21f), u(12f))
                    cubicTo(u(12.5f), u(14.5f), u(12f), u(16.5f), u(12f), u(21.5f))
                    cubicTo(u(12f), u(16.5f), u(11.5f), u(14.5f), u(3f), u(12f))
                    cubicTo(u(11.5f), u(9.5f), u(12f), u(7.5f), u(12f), u(2.5f)); close()
                }
                drawPath(p, color)
            }
            "grok" -> {
                drawLine(color, Offset(u(6f), u(4f)), Offset(u(18f), u(20f)), strokeWidth = 2.2f / 24f * s, cap = StrokeCap.Round)
                drawLine(color, Offset(u(18f), u(4f)), Offset(u(6f), u(20f)), strokeWidth = 2.2f / 24f * s, cap = StrokeCap.Round)
            }
            "mistral" -> {
                val rows = listOf(5f, 9.5f, 14f)
                rows.forEachIndexed { i, ry ->
                    for (c in 0 until 4) {
                        drawRect(color.copy(alpha = if ((i + c) % 2 == 1) 0.55f else 1f),
                            topLeft = Offset(u(3f + c * 4.6f), u(ry)),
                            size = androidx.compose.ui.geometry.Size(u(3.4f), u(3.4f)))
                    }
                }
            }
            "perplexity" -> {
                drawCircle(color, radius = u(7.5f), center = Offset(u(12f), u(12f)), style = Stroke(width = w))
                drawLine(color, Offset(u(12f), u(5f)), Offset(u(12f), u(19f)), strokeWidth = 1.4f / 24f * s, cap = StrokeCap.Round)
                drawLine(color, Offset(u(6f), u(9.5f)), Offset(u(18f), u(14.5f)), strokeWidth = 1.4f / 24f * s, cap = StrokeCap.Round)
                drawLine(color, Offset(u(18f), u(9.5f)), Offset(u(6f), u(14.5f)), strokeWidth = 1.4f / 24f * s, cap = StrokeCap.Round)
            }
            "meta" -> {
                drawCircle(color, radius = u(7f), center = Offset(u(12f), u(12f)), style = Stroke(width = w))
            }
            else -> { // openai
                drawCircle(color, radius = u(7.5f), center = Offset(u(12f), u(12f)), style = Stroke(width = w))
                drawCircle(color, radius = u(2.1f), center = Offset(u(12f), u(12f)), style = Stroke(width = w))
            }
        }
    }
}

private fun formatThousands(n: Int): String {
    val s = n.toString()
    val sb = StringBuilder()
    val len = s.length
    for (i in 0 until len) {
        if (i > 0 && (len - i) % 3 == 0) sb.append(',')
        sb.append(s[i])
    }
    return sb.toString()
}
