package com.yahorshymanchyk.ai_advent_with_love_2.presentation.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ChatsScreen(
    paddingValues: PaddingValues,
    onChatSelected: (Long) -> Unit,
    viewModel: ChatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChatsContent(
        uiState = uiState,
        paddingValues = paddingValues,
        onChatSelected = onChatSelected
    )
}

@Composable
private fun ChatsContent(
    uiState: ChatsUiState,
    paddingValues: PaddingValues,
    onChatSelected: (Long) -> Unit
) {
    when (uiState) {
        is ChatsUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is ChatsUiState.Success -> {
            if (uiState.chats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No chats yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.chats, key = { it.id }) { chat ->
                        ChatItem(chat, onClick = { onChatSelected(chat.id) })
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
        is ChatsUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ChatItem(chat: ChatUiModel, onClick: () -> Unit) {
    Text(
        text = chat.name,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}

@Preview(showBackground = true)
@Composable
private fun ChatsLoadingPreview() {
    ChatsContent(
        uiState = ChatsUiState.Loading,
        paddingValues = PaddingValues(),
        onChatSelected = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ChatsSuccessPreview() {
    ChatsContent(
        uiState = ChatsUiState.Success(
            chats = listOf(
                ChatUiModel(id = 1, name = "Chat with Claude"),
                ChatUiModel(id = 2, name = "Recipe ideas"),
                ChatUiModel(id = 3, name = "Kotlin questions"),
            )
        ),
        paddingValues = PaddingValues(),
        onChatSelected = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ChatsEmptyPreview() {
    ChatsContent(
        uiState = ChatsUiState.Success(chats = emptyList()),
        paddingValues = PaddingValues(),
        onChatSelected = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ChatsErrorPreview() {
    ChatsContent(
        uiState = ChatsUiState.Error(message = "Failed to load chats"),
        paddingValues = PaddingValues(),
        onChatSelected = {}
    )
}
