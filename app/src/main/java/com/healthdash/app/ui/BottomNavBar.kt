package com.healthdash.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthdash.app.AiApp

// label = 하단 바 표시 문구(짧게), navLabel = 사이트 사이드바의 실제 텍스트(이동 매칭용)
data class NavTab(
    val id: String,
    val label: String,
    val navLabel: String,
    val icon: ImageVector,
    val accent: Color
)

// 사이트 왼쪽 사이드바 11개 항목 — 표시 문구는 짧게, 이동은 navLabel(원래 텍스트)로 연결
val NAV_TABS = listOf(
    NavTab("Executive Summary", "Summary", "Executive Summary", Icons.Filled.Dashboard, Color(0xFF7C3AED)),
    NavTab("데일리 기사", "News", "데일리 기사", Icons.Filled.Newspaper, Color(0xFF4F46E5)),
    NavTab("AI 네이티브", "AI Native", "AI 네이티브", Icons.Filled.AutoAwesome, Color(0xFF2563EB)),
    NavTab("빅테크 AI", "빅테크 AI", "빅테크 AI", Icons.Filled.Apartment, Color(0xFF0891B2)),
    NavTab("AI 스타트업", "AI 스타트업", "AI 스타트업", Icons.Filled.RocketLaunch, Color(0xFF0D9488)),
    NavTab("수익화 모델", "Biz Model", "수익화 모델", Icons.Filled.Paid, Color(0xFF059669)),
    NavTab("성능·신뢰성 격차", "신뢰성 Gap", "성능·신뢰성 격차", Icons.Filled.Speed, Color(0xFFD97706)),
    NavTab("리서치 리포트", "Research", "리서치 리포트", Icons.Filled.Science, Color(0xFFEA580C)),
    NavTab("정량 분석", "정량 분석", "정량 분석", Icons.Filled.Analytics, Color(0xFFDB2777)),
    NavTab("분기별 매출 추이", "매출 Trend", "분기별 매출 추이", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFFDC2626)),
    NavTab("주가 차트", "Stock", "주가 차트", Icons.Filled.CandlestickChart, Color(0xFF7E22CE))
)

/** 바 상단 컬러 스트립 (블루 브랜드 그라데이션) */
private val ACCENT_STRIP = listOf(
    Color(0xFF1E40AF), Color(0xFF2563EB), Color(0xFF0891B2),
    Color(0xFF22D3EE), Color(0xFF3B82F6), Color(0xFF1D4ED8), Color(0xFF1E40AF)
)

// 동적 탭(사이트 내비에서 추출)에 순환 배정할 아이콘 / 색상
private val DYNAMIC_ICONS = listOf(
    Icons.Filled.GridView, Icons.Filled.SmartToy, Icons.Filled.Speed, Icons.Filled.PieChart,
    Icons.Filled.Payments, Icons.Filled.Business, Icons.Filled.AutoAwesome,
    Icons.AutoMirrored.Filled.Article, Icons.Filled.Gavel, Icons.Filled.Lightbulb,
    Icons.Filled.Insights, Icons.Filled.Description
)
private val DYNAMIC_ACCENTS = listOf(
    Color(0xFF2563EB), Color(0xFF4F46E5), Color(0xFF0891B2), Color(0xFF0D9488),
    Color(0xFF059669), Color(0xFFD97706), Color(0xFFEA580C), Color(0xFFDB2777),
    Color(0xFFDC2626), Color(0xFF1D4ED8), Color(0xFFC026D3), Color(0xFF7E22CE)
)

/** 사이트 왼쪽 내비에서 추출한 라벨로 하단 탭을 만든다 (id = "idx:N", 클릭은 인덱스 기반). */
fun buildNavTabs(labels: List<String>): List<NavTab> = labels.mapIndexed { i, label ->
    val clean = prettifyNavLabel(label)
    NavTab(
        id = "idx:$i",
        label = clean,
        navLabel = clean,
        icon = DYNAMIC_ICONS[i % DYNAMIC_ICONS.size],
        accent = DYNAMIC_ACCENTS[i % DYNAMIC_ACCENTS.size]
    )
}

// 하단 탭은 사이트 왼쪽 내비를 동적으로 그대로 반영한다(라벨 매칭으로 연결되므로 원본 유지).
private fun prettifyNavLabel(label: String): String = label.trim()

/**
 * 반투명 플로팅 하단 바 — 콘텐츠 위에 떠 있어 뒤가 비쳐 보인다.
 * ◀ ▶ 화살표(고정)로 한 칸씩 이동, 나머지(탭 12개 + AI 로고 4개 + 설정)는 함께 슬라이드.
 * 우측 ˅ 핸들로 접을 수 있고, barScale로 전체 크기 조절(설정/상단 핸들 드래그).
 */
@Composable
fun BottomNavBar(
    activeSection: String,
    barScale: Float,
    tabs: List<NavTab>,
    onTabClick: (NavTab) -> Unit,
    onMoveSection: (Int) -> Unit,
    onAiClick: (AiApp) -> Unit,
    onSettingsClick: () -> Unit,
    onCollapse: () -> Unit,
    onScaleDrag: (Float) -> Unit
) {
    val s = barScale.coerceIn(0.7f, 1.4f)
    val scrollState = rememberScrollState()
    val activeIndex = tabs.indexOfFirst { it.id == activeSection }

    LaunchedEffect(activeIndex, tabs.size) {
        if (activeIndex >= 0 && scrollState.maxValue > 0 && tabs.size > 1) {
            val target = scrollState.maxValue * activeIndex / (tabs.size - 1)
            scrollState.animateScrollTo(target)
        }
    }

    val surface = MaterialTheme.colorScheme.surface
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(surface.copy(alpha = 0.78f), surface.copy(alpha = 0.95f))
                )
            )
    ) {
        // 상단 컬러 스트립 + 크기 조절 핸들 (위로 드래그 = 크게, 아래로 = 작게)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        onScaleDrag(-dragAmount / 260f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Brush.horizontalGradient(ACCENT_STRIP))
            )
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMoveSection(-1) }, enabled = activeIndex > 0) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "이전 탭")
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    BarItem(
                        label = tab.label,
                        accent = tab.accent,
                        active = tab.id == activeSection,
                        scale = s,
                        onClick = { onTabClick(tab) }
                    ) { tint, iconSize ->
                        Icon(tab.icon, contentDescription = tab.label, tint = tint, modifier = Modifier.size(iconSize))
                    }
                }

                BarDivider()

                // AI 4사 로고 버튼 — 탭하면 해당 AI 앱 실행 (선택 텍스트가 있으면 함께 전달)
                AiApp.entries.forEach { app ->
                    BarItem(
                        label = app.displayName,
                        accent = Color(app.color),
                        active = false,
                        alwaysAccentLabel = true,
                        scale = s,
                        onClick = { onAiClick(app) }
                    ) { _, iconSize ->
                        Box(
                            modifier = Modifier
                                .size(iconSize + 6.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(app.iconRes),
                                contentDescription = app.displayName,
                                modifier = Modifier.size(iconSize - 1.dp)
                            )
                        }
                    }
                }

                BarDivider()

                BarItem(
                    label = "설정",
                    accent = MaterialTheme.colorScheme.primary,
                    active = false,
                    scale = s,
                    onClick = onSettingsClick
                ) { tint, iconSize ->
                    Icon(Icons.Filled.Settings, contentDescription = "설정", tint = tint, modifier = Modifier.size(iconSize))
                }
                Spacer(Modifier.width(4.dp))
            }
            IconButton(onClick = { onMoveSection(1) }, enabled = activeIndex < tabs.size - 1) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "다음 탭")
            }
            IconButton(onClick = onCollapse) {
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = "하단 바 접기",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** 접힌 상태에서 하단 바를 다시 펼치는 작은 핸들 */
@Composable
fun CollapsedBarHandle(onExpand: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(bottom = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF1E40AF), Color(0xFF2563EB)))
            )
            .clickable { onExpand() }
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.ExpandLess,
                contentDescription = "하단 바 펼치기",
                tint = Color.White
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "MX AI Insights",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun BarDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .width(1.dp)
            .height(34.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

private fun lighten(c: Color, f: Float) = Color(
    red = c.red + (1f - c.red) * f,
    green = c.green + (1f - c.green) * f,
    blue = c.blue + (1f - c.blue) * f,
    alpha = 1f
)

/**
 * 하단 바 공통 아이템 — 선택 시 아이콘이 그라데이션 원형 배지로 바뀌며
 * 스프링 스케일인 + 은은한 글로우 + 부드러운 바운스(bob) 애니메이션이 적용된다.
 */
@Composable
private fun BarItem(
    label: String,
    accent: Color,
    active: Boolean,
    scale: Float,
    alwaysAccentLabel: Boolean = false,
    onClick: () -> Unit,
    icon: @Composable (Color, androidx.compose.ui.unit.Dp) -> Unit
) {
    val neutral = MaterialTheme.colorScheme.onSurfaceVariant
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    // 실제 버튼처럼: 누르면 통통 줄었다 스프링백
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.82f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "press"
    )
    // 눌릴 때 화려한 액센트 채움 강도(0→1)
    val pressGlow by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "pressGlow"
    )
    val iconTint by animateColorAsState(
        targetValue = if (pressed || active) Color.White else accent.copy(alpha = 0.82f),
        label = "iconTint"
    )
    val labelColor by animateColorAsState(
        targetValue = when {
            pressed -> Color.White
            active || alwaysAccentLabel -> accent
            else -> neutral
        },
        label = "label"
    )
    // 배지 스케일인 (선택)
    val badgeScale by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "badge"
    )
    // 활성 시 위아래로 부드럽게 떠다니는 bob
    val bob by rememberInfiniteTransition(label = "bob").animateFloat(
        initialValue = -1.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bobV"
    )
    // 선택/누름 시 글자 팝(스프링 스케일)
    val labelScale by animateFloatAsState(
        targetValue = if (pressed) 1.12f else if (active) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "labelScale"
    )
    val badgeGrad = Brush.linearGradient(listOf(lighten(accent, 0.2f), accent, accent))
    val pressGrad = Brush.verticalGradient(listOf(lighten(accent, 0.28f), accent))
    val activeFaint = Brush.verticalGradient(listOf(accent.copy(alpha = 0.16f), Color.Transparent))
    val container = (32 * scale).dp

    Column(
        modifier = Modifier
            .padding(horizontal = 2.dp, vertical = (3 * scale).dp)
            .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
            .shadow(
                elevation = (7 * pressGlow).dp,
                shape = RoundedCornerShape(16.dp),
                clip = false,
                ambientColor = accent,
                spotColor = accent
            )
            .clip(RoundedCornerShape(16.dp))
            .drawBehind {
                if (active && pressGlow < 0.99f) drawRect(activeFaint)
                if (pressGlow > 0.001f) drawRect(pressGrad, alpha = pressGlow)
            }
            .clickable(interactionSource = interaction, indication = LocalIndication.current) { onClick() }
            .padding(horizontal = (8 * scale).dp, vertical = (5 * scale).dp)
            .widthIn(min = (46 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 아이콘 + 글자가 선택 시 함께 떠오르고(bob)
        Column(
            modifier = Modifier.graphicsLayer { if (active) translationY = bob.dp.toPx() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(container),
                contentAlignment = Alignment.Center
            ) {
                // 소프트 글로우 (블러 API 없이 radial 그라데이션으로 구현)
                if (badgeScale > 0.01f) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer {
                                scaleX = 1.5f * badgeScale; scaleY = 1.5f * badgeScale; alpha = 0.45f * badgeScale
                            }
                            .background(
                                Brush.radialGradient(listOf(accent.copy(alpha = 0.6f), Color.Transparent)),
                                CircleShape
                            )
                    )
                }
                // 그라데이션 원형 배지 (선택 시 스케일인)
                if (badgeScale > 0.01f) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer { scaleX = badgeScale; scaleY = badgeScale }
                            .clip(CircleShape)
                            .background(badgeGrad)
                    )
                }
                icon(iconTint, (20 * scale).dp)
            }
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                color = labelColor,
                fontSize = (10 * scale).sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.graphicsLayer { scaleX = labelScale; scaleY = labelScale }
            )
        }
    }
}
