package com.healthdash.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.healthdash.app.AiHistoryItem
import com.healthdash.app.data.BookmarkEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

/** 저장된 북마크 목록 바텀시트 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkSheet(
    bookmarks: List<BookmarkEntity>,
    onSelect: (BookmarkEntity) -> Unit,
    onDelete: (BookmarkEntity) -> Unit,
    onAddCurrent: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("북마크", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = onAddCurrent) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("현재 섹션 저장")
                }
            }
            if (bookmarks.isEmpty()) {
                Text(
                    "저장된 북마크가 없습니다",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(bookmarks, key = { it.id }) { bookmark ->
                    ListItem(
                        headlineContent = { Text(bookmark.label) },
                        supportingContent = { Text(DATE_FORMAT.format(Date(bookmark.createdAt))) },
                        leadingContent = {
                            Icon(Icons.Filled.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            IconButton(onClick = { onDelete(bookmark) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "북마크 삭제")
                            }
                        },
                        modifier = Modifier.clickable { onSelect(bookmark) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** AI에 보낸 텍스트 타임라인 바텀시트 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    historyItems: List<AiHistoryItem>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("AI 전송 히스토리", style = MaterialTheme.typography.titleLarge)
            if (historyItems.isEmpty()) {
                Text(
                    "아직 AI로 보낸 텍스트가 없습니다",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(historyItems.reversed()) { item ->
                    ListItem(
                        headlineContent = {
                            Text(item.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        },
                        supportingContent = {
                            Text("${item.app.displayName} · ${DATE_FORMAT.format(Date(item.time))}")
                        }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
