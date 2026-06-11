package com.healthdash.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavTab(val id: String, val label: String, val icon: ImageVector)

val NAV_TABS = listOf(
    NavTab("overview", "오버뷰", Icons.Filled.GridView),
    NavTab("device", "디바이스 헬스", Icons.Filled.Devices),
    NavTab("ai", "AI 네이티브", Icons.Filled.SmartToy),
    NavTab("startup", "체중·피트니스", Icons.Filled.FitnessCenter),
    NavTab("vp", "밸류 프로포지션", Icons.Filled.TrackChanges),
    NavTab("articles", "데일리 기사", Icons.AutoMirrored.Filled.Article),
    NavTab("charts", "정량 분석", Icons.Filled.BarChart),
    NavTab("monthly", "월별 추이", Icons.AutoMirrored.Filled.TrendingUp),
    NavTab("insights", "핵심 인사이트", Icons.Filled.Insights),
    NavTab("dynamics", "경쟁 다이내믹스", Icons.Filled.Map),
    NavTab("bizmodel", "수익화 모델", Icons.Filled.Business),
    NavTab("reports", "리서치 리포트", Icons.Filled.Description)
)

/**
 * 스크롤 가능한 하단 탭 바.
 * 양 끝의 ◀ ▶ 화살표로 탭을 한 칸씩 이동할 수 있다.
 */
@Composable
fun BottomNavBar(
    activeSection: String,
    onTabClick: (NavTab) -> Unit,
    onMoveSection: (Int) -> Unit,
    onBookmarkClick: () -> Unit,
    onSearchClick: () -> Unit,
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
                    val active = tab.id == activeSection
                    val tint = if (active) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onTabClick(tab) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .widthIn(min = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(tab.icon, contentDescription = tab.label, tint = tint, modifier = Modifier.size(22.dp))
                        Text(
                            tab.label,
                            color = tint,
                            fontSize = 10.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
            IconButton(onClick = { onMoveSection(1) }, enabled = activeIndex < NAV_TABS.size - 1) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "다음 탭")
            }
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "검색")
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, contentDescription = "설정")
            }
            IconButton(onClick = onBookmarkClick) {
                Icon(Icons.Filled.Bookmark, contentDescription = "북마크", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
