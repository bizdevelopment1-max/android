package com.healthdash.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthdash.app.AiApp

data class NavTab(val id: String, val label: String, val icon: ImageVector, val accent: Color)

// MX AI Insights 사이트 섹션에 맞춘 탭 (라벨 매칭 폴백으로 사이트 내비와 자동 연동)
val NAV_TABS = listOf(
    NavTab("overview", "오버뷰", Icons.Filled.GridView, Color(0xFF7C3AED)),
    NavTab("models", "AI 모델", Icons.Filled.SmartToy, Color(0xFF4F46E5)),
    NavTab("benchmark", "벤치마크", Icons.Filled.Speed, Color(0xFF0891B2)),
    NavTab("market", "시장 규모", Icons.Filled.PieChart, Color(0xFF0D9488)),
    NavTab("funding", "투자·펀딩", Icons.Filled.Payments, Color(0xFF059669)),
    NavTab("companies", "기업 동향", Icons.Filled.Business, Color(0xFFD97706)),
    NavTab("products", "신제품", Icons.Filled.AutoAwesome, Color(0xFFEA580C)),
    NavTab("research", "연구·논문", Icons.AutoMirrored.Filled.Article, Color(0xFFDB2777)),
    NavTab("policy", "규제·정책", Icons.Filled.Gavel, Color(0xFFDC2626)),
    NavTab("usecases", "활용 사례", Icons.Filled.Lightbulb, Color(0xFF2563EB)),
    NavTab("insights", "핵심 인사이트", Icons.Filled.Insights, Color(0xFFC026D3)),
    NavTab("reports", "리포트", Icons.Filled.Description, Color(0xFF7E22CE))
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

/** 사이트 왼쪽 내비에서 추출한 라벨로 하단 탭을 만든다 (id = "idx:N"). */
fun buildNavTabs(labels: List<String>): List<NavTab> = labels.mapIndexed { i, label ->
    NavTab(
        id = "idx:$i",
        label = label,
        icon = DYNAMIC_ICONS[i % DYNAMIC_ICONS.size],
        accent = DYNAMIC_ACCENTS[i % DYNAMIC_ACCENTS.size]
    )
}

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
                        Image(
                            painter = painterResource(app.iconRes),
                            contentDescription = app.displayName,
                            modifier = Modifier.size(iconSize)
                        )
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

/**
 * 하단 바 공통 아이템 — 선택 시 상단 액센트 인디케이터 + 그라데이션 필 배경 +
 * 스프링 바운스 + 색상 전환 애니메이션.
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
    val iconColor by animateColorAsState(
        targetValue = if (active) accent else accent.copy(alpha = 0.78f),
        label = "icon"
    )
    val labelColor by animateColorAsState(
        targetValue = if (active || alwaysAccentLabel) accent else neutral,
        label = "label"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (active) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )
    val pillBrush = if (active) {
        Brush.verticalGradient(listOf(accent.copy(alpha = 0.22f), accent.copy(alpha = 0.10f)))
    } else {
        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 2.dp, vertical = (4 * scale).dp)
            .clip(RoundedCornerShape(14.dp))
            .background(pillBrush)
            .clickable { onClick() }
            .padding(horizontal = (9 * scale).dp, vertical = (5 * scale).dp)
            .widthIn(min = (44 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 활성 탭 상단 액센트 막대
        AnimatedVisibility(
            visible = active,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 3.dp)
                    .width((18 * scale).dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent)
            )
        }
        Box(Modifier.scale(iconScale)) {
            icon(iconColor, (22 * scale).dp)
        }
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            color = labelColor,
            fontSize = (10 * scale).sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}
