package com.yahorshymanchyk.ai_advent_with_love_2.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatSettingsSheet(
    chatName: String,
    onChatNameChange: (String) -> Unit,
    maxTokensInput: String,
    onMaxTokensChange: (String) -> Unit,
    isMaxTokensValid: Boolean,
    stopSequenceInput: String,
    onStopSequenceChange: (String) -> Unit,
    systemPromptInput: String,
    onSystemPromptChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Chat settings",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Column {
            Text(
                text = "chat name",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = chatName,
                onValueChange = onChatNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            MaxTokensInput(value = maxTokensInput, onValueChange = onMaxTokensChange, isError = !isMaxTokensValid)
            StopSequenceInput(value = stopSequenceInput, onValueChange = onStopSequenceChange)
        }
        SystemPromptInput(value = systemPromptInput, onValueChange = onSystemPromptChange)
    }
}
