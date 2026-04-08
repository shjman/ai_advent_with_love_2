package com.yahorshymanchyk.ai_advent_with_love_2.presentation.home

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    uiState: HomeUiState,
    paddingValues: PaddingValues,
    onSendMessage: () -> Unit,
    onInputTextChange: (String) -> Unit,
    onShowSettings: () -> Unit,
    onDismissSettings: () -> Unit,
    onShowNewChatDialog: () -> Unit,
    onDismissNewChatDialog: () -> Unit,
    onConfirmNewChat: () -> Unit,
    onChatNameChange: (String) -> Unit,
    onMaxTokensChange: (String) -> Unit,
    onStopSequenceChange: (String) -> Unit,
    onSystemPromptChange: (String) -> Unit
) {
    when (uiState) {
        is HomeUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        is HomeUiState.Error -> Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = uiState.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        is HomeUiState.Success -> SuccessHomeView(
            state = uiState,
            paddingValues = paddingValues,
            onSendMessage = onSendMessage,
            onInputTextChange = onInputTextChange,
            onShowSettings = onShowSettings,
            onDismissSettings = onDismissSettings,
            onShowNewChatDialog = onShowNewChatDialog,
            onDismissNewChatDialog = onDismissNewChatDialog,
            onConfirmNewChat = onConfirmNewChat,
            onChatNameChange = onChatNameChange,
            onMaxTokensChange = onMaxTokensChange,
            onStopSequenceChange = onStopSequenceChange,
            onSystemPromptChange = onSystemPromptChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessHomeView(
    state: HomeUiState.Success,
    paddingValues: PaddingValues,
    onSendMessage: () -> Unit,
    onInputTextChange: (String) -> Unit,
    onShowSettings: () -> Unit,
    onDismissSettings: () -> Unit,
    onShowNewChatDialog: () -> Unit,
    onDismissNewChatDialog: () -> Unit,
    onConfirmNewChat: () -> Unit,
    onChatNameChange: (String) -> Unit,
    onMaxTokensChange: (String) -> Unit,
    onStopSequenceChange: (String) -> Unit,
    onSystemPromptChange: (String) -> Unit
) {
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

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        CenterAlignedTopAppBar(
            title = { Text(state.chatName) },
            navigationIcon = {
                IconButton(onClick = onShowSettings) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Chat settings")
                }
            },
            actions = {
                IconButton(onClick = onShowNewChatDialog) {
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
            if (state.isSending) item { LoadingBubble() }
            if (state.sendError != null) item { ErrorBubble(state.sendError) }
        }

        InputSection(
            text = state.inputText,
            onTextChange = onInputTextChange,
            onSend = onSendMessage,
            isSendEnabled = state.inputText.isNotBlank() && isMaxTokensValid && !state.isSending
        )
        TokenFooter(expectedInputTokens = state.expectedInputTokens)
    }

    if (state.showNewChatDialog) {
        AlertDialog(
            onDismissRequest = onDismissNewChatDialog,
            title = { Text("Start new chat?") },
            text = { Text("Current chat will be saved. A new chat will start with default settings.") },
            confirmButton = { TextButton(onClick = onConfirmNewChat) { Text("Yes") } },
            dismissButton = { TextButton(onClick = onDismissNewChatDialog) { Text("No") } }
        )
    }

    if (state.showSettings) {
        ModalBottomSheet(
            onDismissRequest = onDismissSettings,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ChatSettingsSheet(
                chatName = state.chatName,
                onChatNameChange = onChatNameChange,
                maxTokensInput = state.maxTokensInput,
                onMaxTokensChange = onMaxTokensChange,
                isMaxTokensValid = isMaxTokensValid,
                stopSequenceInput = state.stopSequenceInput,
                onStopSequenceChange = onStopSequenceChange,
                systemPromptInput = state.systemPromptInput,
                onSystemPromptChange = onSystemPromptChange
            )
        }
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
