package com.yahorshymanchyk.ai_advent_with_love_2.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SystemPromptInput(value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(
            text = "system_prompt",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("optional", style = MaterialTheme.typography.bodySmall) },
            maxLines = 3,
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}
