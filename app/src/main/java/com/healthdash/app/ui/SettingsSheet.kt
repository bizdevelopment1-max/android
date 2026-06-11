package com.healthdash.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

private val FONT_PRESETS = listOf(70, 85, 100, 120, 150)

/** 글꼴 프리셋, 고대비 모드, TTS 속도, 관심 키워드 설정 바텀시트 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    textZoom: Int,
    onPreset: (Int) -> Unit,
    highContrast: Boolean,
    onHighContrast: (Boolean) -> Unit,
    ttsSpeed: Float,
    onTtsSpeed: (Float) -> Unit,
    keywords: String,
    onKeywords: (String) -> Unit,
    onShowHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("설정", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            Text("글꼴 크기 프리셋 (현재 ${textZoom}%)", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                FONT_PRESETS.forEachIndexed { index, preset ->
                    SegmentedButton(
                        selected = textZoom == preset,
                        onClick = { onPreset(preset) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = FONT_PRESETS.size)
                    ) {
                        Text("$preset")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("고대비 모드 (흰 배경 + 검정 글자)", modifier = Modifier.weight(1f))
                Switch(checked = highContrast, onCheckedChange = onHighContrast)
            }
            Spacer(Modifier.height(16.dp))

            Text(
                "TTS 읽기 속도: ${String.format(Locale.US, "%.1f", ttsSpeed)}x",
                style = MaterialTheme.typography.titleSmall
            )
            Slider(
                value = ttsSpeed,
                onValueChange = onTtsSpeed,
                valueRange = 0.5f..2.0f
            )
            Spacer(Modifier.height(8.dp))

            var keywordInput by remember { mutableStateOf(keywords) }
            OutlinedTextField(
                value = keywordInput,
                onValueChange = {
                    keywordInput = it
                    onKeywords(it)
                },
                label = { Text("관심 키워드 (쉼표로 구분)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onShowHistory) {
                Icon(Icons.Filled.History, contentDescription = null)
                Text("  AI 전송 히스토리 보기")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
