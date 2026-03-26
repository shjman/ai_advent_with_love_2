package com.yahorshymanchyk.ai_advent_with_love_2.presentation.home

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(paddingValues: PaddingValues, viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is HomeUiState.Loading -> LoadingContent(paddingValues)
        is HomeUiState.Error -> ErrorContent(paddingValues, state.message)
        is HomeUiState.Success -> SuccessContent(paddingValues, state, viewModel)
    }
}

@Composable
private fun LoadingContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(paddingValues: PaddingValues, message: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessContent(
    paddingValues: PaddingValues,
    state: HomeUiState.Success,
    viewModel: HomeViewModel
) {
    var inputText by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val clipboard = LocalClipboard.current
    val clipboardScope = rememberCoroutineScope()
    val context = LocalContext.current

    val isMaxTokensValid = state.maxTokensInput.toIntOrNull()?.let { it > 0 } ?: false
    val itemCount = state.messages.size +
            (if (state.isSending) 1 else 0) +
            (if (state.sendError != null) 1 else 0)

    LaunchedEffect(itemCount) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        CenterAlignedTopAppBar(
            title = { Text(state.chatName) },
            navigationIcon = {
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Chat settings")
                }
            },
            actions = {
                IconButton(onClick = { showNewChatDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "New chat")
                }
            },
            modifier = Modifier.height(56.dp),
            windowInsets = WindowInsets(0)
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(state.messages) { index, message ->
                MessageBubble(
                    message = message,
                    onLongClick = {
                        val text = buildQAText(state.messages, index)
                        clipboardScope.launch {
                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(null, text)))
                        }
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            if (state.isSending) {
                item { LoadingBubble() }
            }
            if (state.sendError != null) {
                item { ErrorBubble(state.sendError) }
            }
        }

        InputSection(
            text = inputText,
            onTextChange = { inputText = it },
            onSend = {
                viewModel.sendMessage(inputText)
                inputText = ""
            },
            isSendEnabled = inputText.isNotBlank() && isMaxTokensValid && !state.isSending
        )
        TokenFooter(expectedInputTokens = state.expectedInputTokens)
    }

    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("Start new chat?") },
            text = { Text("Current chat will be saved. A new chat will start with default settings.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.startNewChat()
                    showNewChatDialog = false
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) { Text("No") }
            }
        )
    }

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ChatSettingsSheet(
                chatName = state.chatName,
                onChatNameChange = viewModel::updateChatName,
                maxTokensInput = state.maxTokensInput,
                onMaxTokensChange = viewModel::updateMaxTokens,
                isMaxTokensValid = isMaxTokensValid,
                stopSequenceInput = state.stopSequenceInput,
                onStopSequenceChange = viewModel::updateStopSequence,
                systemPromptInput = state.systemPromptInput,
                onSystemPromptChange = viewModel::updateSystemPrompt
            )
        }
    }
}

@Composable
private fun ChatSettingsSheet(
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
            MaxTokensInput(
                value = maxTokensInput,
                onValueChange = onMaxTokensChange,
                isError = !isMaxTokensValid
            )
            StopSequenceInput(
                value = stopSequenceInput,
                onValueChange = onStopSequenceChange
            )
        }
        SystemPromptInput(
            value = systemPromptInput,
            onValueChange = onSystemPromptChange
        )
    }
}

private fun buildQAText(messages: List<MessageUiModel>, index: Int): String {
    val message = messages[index]
    return if (message.isFromUser) {
        val answer = messages.getOrNull(index + 1)?.takeIf { !it.isFromUser }?.content
        if (answer != null) "Q: ${message.content}\n\nA: $answer" else "Q: ${message.content}"
    } else {
        val question = messages.getOrNull(index - 1)?.takeIf { it.isFromUser }?.content
        if (question != null) "Q: $question\n\nA: ${message.content}" else "A: ${message.content}"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(message: MessageUiModel, onLongClick: () -> Unit) {
    val isUser = message.isFromUser
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
            color = if (isUser) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .combinedClickable(onClick = {}, onLongClick = onLongClick)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = if (isUser) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MaxTokensInput(value: String, onValueChange: (String) -> Unit, isError: Boolean) {
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
private fun StopSequenceInput(value: String, onValueChange: (String) -> Unit) {
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
private fun SystemPromptInput(value: String, onValueChange: (String) -> Unit) {
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

@Composable
private fun LoadingBubble() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            CircularProgressIndicator(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).size(20.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun ErrorBubble(error: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
        Button(onClick = onSend, enabled = isSendEnabled) {
            Text("Send")
        }
    }
}

@Composable
private fun TokenFooter(expectedInputTokens: Int?) {
    val text = if (expectedInputTokens != null) "expected input tokens base on history: $expectedInputTokens" else ""
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
