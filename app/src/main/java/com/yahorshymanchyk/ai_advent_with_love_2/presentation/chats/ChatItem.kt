package com.yahorshymanchyk.ai_advent_with_love_2.presentation.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatItem(chat: ChatUiModel, onClick: () -> Unit) {
    Text(
        text = chat.name,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}
