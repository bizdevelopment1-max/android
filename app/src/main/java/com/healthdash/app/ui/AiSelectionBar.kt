package com.healthdash.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.healthdash.app.AiApp

/**
 * 텍스트 선택 시 슬라이드업되는 AI 선택 바.
 * 탭: 로그인된 네이티브 AI 앱을 분할 화면으로 실행 + 텍스트 전달
 * 길게 누르기: 공유 시트로 원하는 앱 직접 선택
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AiSelectionBar(
    modifier: Modifier = Modifier,
    selectedText: String,
    onAiClick: (AiApp) -> Unit,
    onAiLongClick: (AiApp) -> Unit,
    onTts: () -> Unit,
    onTranslate: () -> Unit,
    onHighlight: () -> Unit
) {
    AnimatedVisibility(
        visible = selectedText.isNotBlank(),
        modifier = modifier,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = "“" + selectedText.take(80) + (if (selectedText.length > 80) "…" else "") + "”",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AiApp.entries.forEach { app ->
                        Surface(
                            color = Color(app.color),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.combinedClickable(
                                onClick = { onAiClick(app) },
                                onLongClick = { onAiLongClick(app) }
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Image(
                                        painter = painterResource(app.iconRes),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .padding(3.dp)
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    app.displayName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                    IconButton(onClick = onTts) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "읽어주기")
                    }
                    IconButton(onClick = onTranslate) {
                        Icon(Icons.Filled.Translate, contentDescription = "번역")
                    }
                    IconButton(onClick = onHighlight) {
                        Icon(Icons.Filled.BorderColor, contentDescription = "하이라이트 저장")
                    }
                }
            }
        }
    }
}
