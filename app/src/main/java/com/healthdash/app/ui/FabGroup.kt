package com.healthdash.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** 글자 확대/축소, 공유, 스크린샷 FAB 그룹 (잠시 후 반투명 페이드). 화면 회전은 센서 자동. */
@Composable
fun FabGroup(
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onShare: () -> Unit,
    onScreenshot: () -> Unit
) {
    var bright by remember { mutableStateOf(true) }
    var wakeTick by remember { mutableStateOf(0) }
    val alpha by animateFloatAsState(targetValue = if (bright) 1f else 0.45f, label = "fabAlpha")

    LaunchedEffect(wakeTick) {
        bright = true
        delay(2000)
        bright = false
    }

    fun wake(action: () -> Unit): () -> Unit = {
        wakeTick++
        action()
    }

    Column(
        modifier = modifier.alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SmallFloatingActionButton(onClick = wake(onZoomIn)) {
            Icon(Icons.Filled.TextIncrease, contentDescription = "글자 확대")
        }
        SmallFloatingActionButton(onClick = wake(onZoomOut)) {
            Icon(Icons.Filled.TextDecrease, contentDescription = "글자 축소")
        }
        SmallFloatingActionButton(onClick = wake(onShare)) {
            Icon(Icons.Filled.Share, contentDescription = "공유")
        }
        SmallFloatingActionButton(onClick = wake(onScreenshot)) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = "스크린샷 저장")
        }
    }
}
