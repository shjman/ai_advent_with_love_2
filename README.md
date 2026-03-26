# AI Advent with Love

An Android chat application powered by the Anthropic Claude API. Supports multiple persistent chat sessions with per-chat settings, built with Jetpack Compose and Clean Architecture across a multi-module Gradle project.

## Features

- **Multi-session chat** — create and manage multiple independent chat sessions
- **Persistent history** — all messages and settings stored locally in a Room database, survive app restarts
- **Per-chat configuration** — each chat stores its own name, max tokens, system prompt, and stop sequence
- **Chat switching** — tap any chat in the Chats tab to open it in the Home tab instantly
- **Rename chats** — edit the chat name directly from the settings sheet
- **Bottom navigation** — Home (active chat), Chats (session list), Settings tabs
- **Chat settings sheet** — half-screen bottom sheet for all per-chat parameters, opened via the ⋮ button
- **New chat** — start a fresh session (with confirmation dialog) via the + button in the toolbar
- **Message copy** — long-press any message bubble to copy the Q&A pair to clipboard
- **Expected token count** — footer shows the pre-flight token estimate for the current conversation history
- **Typed UI states** — Home screen has distinct `Loading`, `Success`, and `Error` states
- **Material 3** — dynamic color (Android 12+), dark/light theme support

## Tech Stack

| Area | Library / Version |
|------|-------------------|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material 3 (BOM 2024.09.00) |
| Navigation | Navigation Compose 2.8.0 |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt 2.59.2 + KSP 2.2.10-2.0.2 |
| Database | Room 2.7.0 |
| Async | Kotlin Coroutines + Flow 1.10.1 |
| Network | OkHttp 4.12.0 |
| AI | Anthropic Java SDK 0.8.0 (claude-haiku-4-5-20251001) |
| Logging | Timber 5.0.1 |
| Build | AGP 9.1.0, minSdk 24, targetSdk 36 |

## Project Structure

```
.
├── domain-models/                      # Pure Kotlin JVM — no Android deps
│   └── src/main/kotlin/.../domain/model/
│       ├── Chat.kt
│       └── ChatMessage.kt
│
├── feature-claude/                     # Self-contained Claude API feature
│   └── src/main/java/.../
│       ├── data/remote/
│       │   └── ClaudeApiService.kt     # Anthropic SDK wrapper (send + countTokens)
│       ├── data/repository/
│       │   └── ClaudeRepositoryImpl.kt
│       ├── domain/repository/
│       │   └── ClaudeRepository.kt
│       ├── domain/usecase/
│       │   └── SendMessageUseCase.kt
│       └── di/
│           └── ClaudeModule.kt         # Hilt: AnthropicClient, service, repository, use case
│
├── database/                           # Room database module
│   └── src/main/java/.../database/
│       ├── AppDatabase.kt
│       ├── entity/
│       │   ├── ChatEntity.kt           # chats table
│       │   └── MessageEntity.kt        # messages table (FK → chats, CASCADE delete)
│       └── dao/
│           ├── ChatDao.kt
│           └── MessageDao.kt
│
└── app/                                # Presentation layer
    └── src/main/java/.../
        ├── presentation/
        │   ├── navigation/
        │   │   ├── AppNavigation.kt    # Single Scaffold + NavHost + BottomNavigationBar
        │   │   └── Screen.kt           # Bottom tab route definitions
        │   ├── home/
        │   │   ├── HomeScreen.kt       # Loading / Error / Success states
        │   │   ├── HomeViewModel.kt
        │   │   ├── HomeUiState.kt      # Sealed class: Loading, Success, Error
        │   │   └── MessageUiModel.kt   # UI model + ChatMessage.toUiModel() mapper
        │   ├── chats/
        │   │   ├── ChatsScreen.kt      # Clickable chat list
        │   │   ├── ChatsViewModel.kt
        │   │   └── ChatUiModel.kt      # UI model + Chat.toUiModel() mapper
        │   └── settings/
        │       └── SettingsScreen.kt   # Placeholder
        ├── domain/
        │   └── repository/
        │       └── ChatRepository.kt   # Local persistence interface
        ├── data/
        │   └── local/
        │       └── ChatRepositoryImpl.kt  # Room-backed implementation
        └── di/
            └── DatabaseModule.kt       # Hilt: AppDatabase, DAOs, ChatRepository
```

## Architecture

Four Gradle modules with a strict dependency graph:

```
:domain-models  (pure Kotlin JVM — no deps)
       ↑
:feature-claude   :database
       ↑               ↑
            :app
```

**Home screen UI state (sealed class):**
```
HomeUiState
  ├── Loading   — shown during initial load, chat switch, new chat creation
  ├── Success   — chat is active; holds messages, settings, isSending, sendError, expectedInputTokens
  └── Error     — fatal failure (DB error, chat not found)
```

**Send message flow:**
```
HomeScreen → HomeViewModel.sendMessage()
  → ChatRepository.saveMessage()         // persist user message
  → ChatRepository.updateChatSettings()  // persist settings
  → SendMessageUseCase()
      → ClaudeRepository.sendMessage()
          → ClaudeApiService             // Anthropic SDK — logs in/out tokens + elapsed ms
  → ChatRepository.saveMessage()         // persist assistant response
```

**Chat load / switch flow:**
```
HomeViewModel.init / loadChat(chatId)
  → ChatRepository.getLatestChat() / getChatById()
  → currentChatId (MutableStateFlow)
      → flatMapLatest → getMessagesForChat() Flow
          → HomeUiState.Success.messages        // auto-updates from DB
          → claudeRepository.countTokens()      // updates expectedInputTokens footer
```

**UI models vs domain models:**
Domain models (`Chat`, `ChatMessage`) live in `:domain-models` and are used across all modules. UI models (`MessageUiModel`, `ChatUiModel`) live in `:app` next to their screens, with mapper extension functions. ViewModels map domain → UI on the way in, and reverse-map UI → domain when calling the Claude API.

## Setup

1. Clone the repository.
2. Create `local.properties` in the project root (if not already present) and add your Anthropic API key:
   ```
   CLAUDE_API_KEY=your_key_here
   ```
3. Build and run:
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

## Build Commands

```bash
./gradlew assembleDebug       # Build debug APK
./gradlew assembleRelease     # Build release APK
./gradlew testDebugUnitTest   # Run unit tests
./gradlew lint                # Run lint checks
./gradlew installDebug        # Build and install on connected device/emulator
```

## Database Schema

**`chats`**

| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER | Primary key, auto-generated |
| name | TEXT | Editable chat name |
| maxTokens | INTEGER | Per-chat token limit |
| systemPrompt | TEXT | Nullable |
| stopSequence | TEXT | Nullable |
| createdAt | INTEGER | Unix timestamp ms |
| updatedAt | INTEGER | Unix timestamp ms, used for ordering |

**`messages`**

| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER | Primary key, auto-generated |
| chatId | INTEGER | Foreign key → chats.id (CASCADE delete) |
| role | TEXT | `"user"` or `"assistant"` |
| content | TEXT | Message body |
| timestamp | INTEGER | Unix timestamp ms |
