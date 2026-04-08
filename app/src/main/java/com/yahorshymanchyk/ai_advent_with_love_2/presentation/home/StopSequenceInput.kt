package com.yahorshymanchyk.ai_advent_with_love_2.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StopSequenceInput(value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(
            text = "stopSequence",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(140.dp),
            singleLine = true,
            placeholder = { Text("optional", style = MaterialTheme.typography.bodySmall) },
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}
