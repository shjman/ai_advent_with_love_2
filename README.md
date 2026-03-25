# AI Advent with Love

An Android chat application powered by the Anthropic Claude API. Supports multiple persistent chat sessions with per-chat settings, built with Jetpack Compose and Clean Architecture.

## Features

- **Multi-session chat** — create and manage multiple independent chat sessions
- **Persistent history** — all messages and settings are stored locally in a Room database and survive app restarts
- **Per-chat configuration** — each chat stores its own name, max tokens, system prompt, and stop sequence
- **Rename chats** — edit the chat name directly from the settings sheet
- **Bottom navigation** — Home (active chat), Chats (session list), Settings tabs
- **Chat settings sheet** — half-screen bottom sheet for all per-chat parameters, opened via the ⋮ button
- **New chat** — start a fresh session (with confirmation dialog) via the + button in the toolbar
- **Message copy** — long-press any message bubble to copy the Q&A pair to clipboard
- **Material 3** — dynamic color (Android 12+), dark/light theme support

## Tech Stack

| Area | Library / Version |
|---|---|
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
├── app/                          # Main application module
│   └── src/main/java/.../
│       ├── presentation/
│       │   ├── AppNavigation.kt  # Single Scaffold + NavHost + BottomNavigationBar
│       │   ├── Screen.kt         # Bottom tab route definitions
│       │   ├── ClaudeScreen.kt   # Active chat UI (toolbar, messages, input, sheets)
│       │   ├── ClaudeViewModel.kt
│       │   ├── ClaudeUiState.kt
│       │   ├── ChatsScreen.kt    # Persisted chat session list
│       │   ├── ChatsViewModel.kt
│       │   └── SettingsScreen.kt # Placeholder
│       ├── domain/
│       │   ├── model/
│       │   │   ├── Chat.kt       # Chat session domain model
│       │   │   └── ChatMessage.kt
│       │   ├── repository/
│       │   │   ├── ChatRepository.kt   # Local persistence interface
│       │   │   └── ClaudeRepository.kt # API interface
│       │   └── usecase/
│       │       └── SendMessageUseCase.kt
│       ├── data/
│       │   ├── local/
│       │   │   └── ChatRepositoryImpl.kt  # Room-backed implementation
│       │   ├── remote/
│       │   │   └── ClaudeApiService.kt    # Anthropic SDK wrapper
│       │   └── repository/
│       │       └── ClaudeRepositoryImpl.kt
│       └── di/
│           ├── AppModule.kt      # Anthropic client + use case wiring
│           ├── DatabaseModule.kt # Room DB + DAO + ChatRepository wiring
│           └── NetworkModule.kt  # OkHttpClient
│
└── database/                     # Room database module
    └── src/main/java/.../database/
        ├── AppDatabase.kt
        ├── entity/
        │   ├── ChatEntity.kt     # chats table
        │   └── MessageEntity.kt  # messages table (FK → chats, CASCADE delete)
        └── dao/
            ├── ChatDao.kt
            └── MessageDao.kt
```

## Architecture

Clean Architecture layered across two Gradle modules:

```
:app  presentation  →  domain  →  data
                           ↑
                       :database (Room entities + DAOs)
```

**Send message flow:**
```
ClaudeScreen
  → ClaudeViewModel.sendMessage()
      → ChatRepository.saveMessage()       // persist user message
      → ChatRepository.updateChatSettings() // persist settings
      → SendMessageUseCase()               // call Claude API
          → ClaudeRepository.sendMessage()
              → ClaudeApiService           // Anthropic SDK
      → ChatRepository.saveMessage()       // persist assistant response
```

**Chat load flow (on launch / new chat):**
```
ClaudeViewModel.init
  → ChatRepository.getLatestChat()   // or createChat()
  → currentChatId (MutableStateFlow)
      → flatMapLatest → getMessagesForChat() Flow
          → ClaudeUiState.messages   // auto-updates from DB writes
```

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
|---|---|---|
| id | INTEGER | Primary key, auto-generated |
| name | TEXT | Editable chat name |
| maxTokens | INTEGER | Per-chat token limit |
| systemPrompt | TEXT | Nullable |
| stopSequence | TEXT | Nullable |
| createdAt | INTEGER | Unix timestamp ms |
| updatedAt | INTEGER | Unix timestamp ms, used for ordering |

**`messages`**

| Column | Type | Notes |
|---|---|---|
| id | INTEGER | Primary key, auto-generated |
| chatId | INTEGER | Foreign key → chats.id (CASCADE delete) |
| role | TEXT | `"user"` or `"assistant"` |
| content | TEXT | Message body |
| timestamp | INTEGER | Unix timestamp ms |
