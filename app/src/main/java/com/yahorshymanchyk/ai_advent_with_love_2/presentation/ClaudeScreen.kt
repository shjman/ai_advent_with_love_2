package com.yahorshymanchyk.ai_advent_with_love_2.presentation

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yahorshymanchyk.ai_advent_with_love_2.domain.model.ChatMessage

@Composable
fun ClaudeScreen(paddingValues: PaddingValues, viewModel: ClaudeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    var maxTokensInput by remember { mutableStateOf("512") }
    var stopSequenceInput by remember { mutableStateOf("") }
    var systemPromptInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val maxTokensValue = maxTokensInput.toIntOrNull()
    val isMaxTokensValid = maxTokensValue != null && maxTokensValue > 0

    val error = uiState.error
    val itemCount = uiState.messages.size +
            (if (uiState.isLoading) 1 else 0) +
            (if (error != null) 1 else 0)

    LaunchedEffect(itemCount) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(uiState.messages) { index, message ->
                    MessageBubble(
                        message = message,
                        onLongClick = {
                            val text = buildQAText(uiState.messages, index)
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                if (uiState.isLoading) {
                    item { LoadingBubble() }
                }
                if (error != null) {
                    item { ErrorBubble(error) }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                MaxTokensInput(
                    value = maxTokensInput,
                    onValueChange = { maxTokensInput = it },
                    isError = !isMaxTokensValid
                )
                StopSequenceInput(
                    value = stopSequenceInput,
                    onValueChange = { stopSequenceInput = it }
                )
            }
            SystemPromptInput(
                value = systemPromptInput,
                onValueChange = { systemPromptInput = it }
            )
            InputSection(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    viewModel.sendMessage(
                        inputText,
                        maxTokensValue!!,
                        stopSequenceInput.takeIf { it.isNotBlank() },
                        systemPromptInput.takeIf { it.isNotBlank() }
                    )
                    inputText = ""
                },
                isSendEnabled = inputText.isNotBlank() && isMaxTokensValid && !uiState.isLoading
            )
            PoweredByFooter()
    }
}

private fun buildQAText(messages: List<ChatMessage>, index: Int): String {
    val message = messages[index]
    return when (message.role) {
        ChatMessage.Role.USER -> {
            val answer = messages.getOrNull(index + 1)
                ?.takeIf { it.role == ChatMessage.Role.ASSISTANT }
                ?.content
            if (answer != null) "Q: ${message.content}\n\nA: $answer"
            else "Q: ${message.content}"
        }
        ChatMessage.Role.ASSISTANT -> {
            val question = messages.getOrNull(index - 1)
                ?.takeIf { it.role == ChatMessage.Role.USER }
                ?.content
            if (question != null) "Q: $question\n\nA: ${message.content}"
            else "A: ${message.content}"
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(message: ChatMessage, onLongClick: () -> Unit) {
    val isUser = message.role == ChatMessage.Role.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = if (isUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MaxTokensInput(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    Column {
        Text(
            text = "maxTokens",
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(100.dp),
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.bodyMedium
        )
        if (isError) {
            Text(
                text = "invalid input",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun StopSequenceInput(
    value: String,
    onValueChange: (String) -> Unit
) {
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

@Composable
private fun SystemPromptInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
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

@Composable
private fun LoadingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .size(20.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun ErrorBubble(error: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Text(
                text = error,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun InputSection(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isSendEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...") },
            maxLines = 4
        )
        Button(
            onClick = onSend,
            enabled = isSendEnabled
        ) {
            Text("Send")
        }
    }
}

@Composable
private fun PoweredByFooter() {
    Text(
        text = "powered by claude",
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
