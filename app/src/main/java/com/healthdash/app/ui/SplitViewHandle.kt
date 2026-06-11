package com.healthdash.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthdash.app.AiApp

/** 스플릿 뷰 드래그 핸들 — 수직 드래그로 비율 조절, 더블탭 시 기본 비율 복원, X로 닫기 */
@Composable
fun SplitViewHandle(
    onDrag: (Float) -> Unit,
    onDoubleTap: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onDoubleTap() })
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(48.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Icon(
            Icons.Filled.Close,
            contentDescription = "스플릿 뷰 닫기",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .size(20.dp)
                .clickable { onClose() }
        )
    }
}

/** 스플릿 뷰 상단의 AI 앱 전환 탭 바 */
@Composable
fun AiPanelTopBar(
    current: AiApp,
    onSwitch: (AiApp) -> Unit,
    onHistory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AiApp.entries.forEach { app ->
            val active = app == current
            TextButton(onClick = { onSwitch(app) }) {
                Text(
                    app.displayName,
                    color = if (active) Color(app.color) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onHistory) {
            Icon(Icons.Filled.History, contentDescription = "AI 전송 히스토리")
        }
    }
}
