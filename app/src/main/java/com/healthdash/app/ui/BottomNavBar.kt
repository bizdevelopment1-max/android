package com.healthdash.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthdash.app.AiApp

data class NavTab(val id: String, val label: String, val icon: ImageVector, val accent: Color)

val NAV_TABS = listOf(
    NavTab("overview", "오버뷰", Icons.Filled.GridView, Color(0xFF1428A0)),
    NavTab("device", "디바이스 헬스", Icons.Filled.Devices, Color(0xFF0277BD)),
    NavTab("ai", "AI 네이티브", Icons.Filled.SmartToy, Color(0xFF7B1FA2)),
    NavTab("startup", "체중·피트니스", Icons.Filled.FitnessCenter, Color(0xFF2E7D32)),
    NavTab("vp", "밸류 프로포지션", Icons.Filled.TrackChanges, Color(0xFFC62828)),
    NavTab("articles", "데일리 기사", Icons.AutoMirrored.Filled.Article, Color(0xFFEF6C00)),
    NavTab("charts", "정량 분석", Icons.Filled.BarChart, Color(0xFF00838F)),
    NavTab("monthly", "월별 추이", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF6D4C41)),
    NavTab("insights", "핵심 인사이트", Icons.Filled.Insights, Color(0xFFAD1457)),
    NavTab("dynamics", "경쟁 다이내믹스", Icons.Filled.Map, Color(0xFF283593)),
    NavTab("bizmodel", "수익화 모델", Icons.Filled.Business, Color(0xFF00695C)),
    NavTab("reports", "리서치 리포트", Icons.Filled.Description, Color(0xFF6A1B9A))
)

/**
 * 하단 바 — ◀ ▶ 화살표(고정)를 제외한 모든 항목이 함께 슬라이드:
 * 섹션 탭 12개(탭별 고유 색상 + 선택 애니메이션) + AI 4사 로고 + 설정.
 */
@Composable
fun BottomNavBar(
    activeSection: String,
    onTabClick: (NavTab) -> Unit,
    onMoveSection: (Int) -> Unit,
    onAiClick: (AiApp) -> Unit,
    onSettingsClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val activeIndex = NAV_TABS.indexOfFirst { it.id == activeSection }

    // 활성 탭이 바뀌면 해당 위치로 탭 바를 자동 스크롤
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && scrollState.maxValue > 0 && NAV_TABS.size > 1) {
            val target = scrollState.maxValue * activeIndex / (NAV_TABS.size - 1)
            scrollState.animateScrollTo(target)
        }
    }

    Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
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
                NAV_TABS.forEach { tab ->
                    BarItem(
                        label = tab.label,
                        accent = tab.accent,
                        active = tab.id == activeSection,
                        onClick = { onTabClick(tab) }
                    ) { tint ->
                        Icon(tab.icon, contentDescription = tab.label, tint = tint, modifier = Modifier.size(22.dp))
                    }
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(36.dp)
                        .padding(horizontal = 6.dp)
                )

                // AI 4사 로고 버튼 — 탭하면 해당 AI 앱 실행 (선택 텍스트가 있으면 함께 전달)
                AiApp.entries.forEach { app ->
                    BarItem(
                        label = app.displayName,
                        accent = Color(app.color),
                        active = false,
                        alwaysAccentLabel = true,
                        onClick = { onAiClick(app) }
                    ) { _ ->
                        Image(
                            painter = painterResource(app.iconRes),
                            contentDescription = app.displayName,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(36.dp)
                        .padding(horizontal = 6.dp)
                )

                BarItem(
                    label = "설정",
                    accent = MaterialTheme.colorScheme.primary,
                    active = false,
                    onClick = onSettingsClick
                ) { tint ->
                    Icon(Icons.Filled.Settings, contentDescription = "설정", tint = tint, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(4.dp))
            }
            IconButton(onClick = { onMoveSection(1) }, enabled = activeIndex < NAV_TABS.size - 1) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "다음 탭")
            }
        }
    }
}

/**
 * 하단 바 공통 아이템 — 선택 시 고유 색상 필 배경 + 스프링 바운스 + 색상 전환 애니메이션
 */
@Composable
private fun BarItem(
    label: String,
    accent: Color,
    active: Boolean,
    alwaysAccentLabel: Boolean = false,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit
) {
    val neutral = MaterialTheme.colorScheme.onSurfaceVariant
    val pillColor by animateColorAsState(
        targetValue = if (active) accent.copy(alpha = 0.15f) else Color.Transparent,
        label = "pill"
    )
    val contentColor by animateColorAsState(
        targetValue = if (active || alwaysAccentLabel) accent else neutral,
        label = "content"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (active) 1.18f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(pillColor)
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 6.dp)
            .widthIn(min = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.scale(iconScale)) {
            icon(contentColor)
        }
        Text(
            label,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}
