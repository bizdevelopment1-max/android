package com.healthdash.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * 우측 플로팅 버튼 6개 — 글자 +/−, 스크롤 ▲/▼, 페이지 업/다운.
 * 녹색 계열 다색, 작고 반투명하며 잠시 후 더 투명해진다.
 * 그룹 전체를 드래그해서 위아래 위치를 옮길 수 있다.
 */
@Composable
fun FabGroup(
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onScrollUp: () -> Unit,
    onScrollDown: () -> Unit,
    onPageUp: () -> Unit,
    onPageDown: () -> Unit,
    onRefresh: () -> Unit
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    var wakeTick by remember { mutableIntStateOf(0) }
    var bright by remember { mutableStateOf(true) }
    val alpha by animateFloatAsState(targetValue = if (bright) 0.9f else 0.28f, label = "fabAlpha")

    LaunchedEffect(wakeTick) {
        bright = true
        delay(1800)
        bright = false
    }

    fun wake(action: () -> Unit): () -> Unit = {
        wakeTick++
        action()
    }

    Column(
        modifier = modifier
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .alpha(alpha)
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    offsetY += drag.y
                    wakeTick++
                }
            },
        verticalArrangement = Arrangement.spacedBy(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FabButton(Icons.Filled.Add, Color(0xFF2E7D32), "글자 확대", wake(onZoomIn))
        FabButton(Icons.Filled.Remove, Color(0xFF00897B), "글자 축소", wake(onZoomOut))
        FabButton(Icons.Filled.KeyboardArrowUp, Color(0xFF43A047), "위로 스크롤", wake(onScrollUp))
        FabButton(Icons.Filled.KeyboardArrowDown, Color(0xFF1B7A43), "아래로 스크롤", wake(onScrollDown))
        FabButton(Icons.Filled.KeyboardDoubleArrowUp, Color(0xFF00ACC1), "페이지 업", wake(onPageUp))
        FabButton(Icons.Filled.KeyboardDoubleArrowDown, Color(0xFF7CB342), "페이지 다운", wake(onPageDown))
        FabButton(Icons.Filled.Refresh, Color(0xFF0288D1), "새로고침", wake(onRefresh))
    }
}

@Composable
private fun FabButton(
    icon: ImageVector,
    tint: Color,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.20f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(18.dp))
    }
}
