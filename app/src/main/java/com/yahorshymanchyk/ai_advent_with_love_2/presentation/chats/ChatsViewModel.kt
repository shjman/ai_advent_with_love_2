package com.yahorshymanchyk.ai_advent_with_love_2.presentation.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yahorshymanchyk.ai_advent_with_love_2.domain.usecase.GetAllChatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    getAllChatsUseCase: GetAllChatsUseCase
) : ViewModel() {

    val uiState: StateFlow<ChatsUiState> = getAllChatsUseCase()
        .map<_, ChatsUiState> { chats -> ChatsUiState.Success(chats.map { it.toUiModel() }) }
        .catch { emit(ChatsUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChatsUiState.Loading
        )
}
