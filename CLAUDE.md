# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Working Style

Before starting any implementation:
1. Read and analyze the request carefully.
2. Ask clarifying questions — as many as needed — until requirements are unambiguous. Do not assume; ask. Prefer multiple short rounds of questions over starting with incomplete information.
3. If you have multiple approaches to solve the problem, outline them and ask which one to pursue rather than choosing silently.
4. If the request is large or complex, break it down into smaller sub-tasks and ask for approval on the breakdown before proceeding.
5. If the request involves modifying existing code, review the relevant code sections first and ask any questions needed to understand the current implementation before proposing changes.
6. If you have some disagreement with requirement or think there is a better way to achieve the underlying goal, explain your perspective and ask if you should proceed with the original requirements or your proposed alternative.
7. Present a detailed step-by-step plan and wait for explicit approval before writing any code.
8. If a decision arises during planning that has meaningful alternatives, surface it and ask which direction to take rather than choosing silently.

During implementation:
- Proceed step by step. After each significant decision point or unexpected finding, pause and check in with the user before continuing.
- If something discovered mid-implementation changes the original plan, stop and discuss rather than adapting unilaterally.

Once complete, verify the solution compiles by running `./gradlew assembleDebug`.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew testDebugUnitTest      # Run unit tests (debug)
./gradlew lint                   # Run lint checks
./gradlew installDebug           # Build and install on connected device/emulator
```

## Modules

| Module      | Purpose |
|-------------|---------|
| `:app`      | Presentation, domain, data layers; DI wiring |
| `:database` | Room database — entities, DAOs, `AppDatabase` |

Module path constants are declared as `val` at the top of each `build.gradle.kts` (e.g. `val moduleDatabase = ":database"`) — never hardcode module name strings in dependency declarations.

## Architecture

Clean Architecture + MVVM across `:app` and `:database` modules.

```
:app
  presentation/   →  domain/          →  data/
  ClaudeScreen        model               ClaudeApiService
  ClaudeViewModel     repository          ClaudeRepositoryImpl
  ChatsScreen         usecase             ChatRepositoryImpl
  ChatsViewModel
  AppNavigation

:database
  entity/   →  dao/       →  AppDatabase
  ChatEntity   ChatDao
  MessageEntity  MessageDao
```

**Message send flow:** `ClaudeScreen` → `ClaudeViewModel.sendMessage()` → saves user msg to DB → `SendMessageUseCase` → `ClaudeRepository` → `ClaudeApiService` → Anthropic SDK → saves assistant msg to DB

**Chat persistence flow:** `ClaudeViewModel` init → `ChatRepository.getLatestChat()` (or `createChat()`) → collects `getMessagesForChat()` Flow → updates `ClaudeUiState`

**State:** `ClaudeViewModel` holds `StateFlow<ClaudeUiState>` (chatId, chatName, messages, settings, isLoading, error). Messages are driven by a Room Flow so they auto-update from DB writes.

**DI:** `AppModule` wires Anthropic stack. `DatabaseModule` wires `AppDatabase` → DAOs → `ChatRepository`. `NetworkModule` provides a singleton `OkHttpClient`.

## Key Build Constraints

- **AGP 9.1.0 built-in Kotlin:** Do NOT apply `kotlin-android` plugin in `app/build.gradle.kts` — it conflicts with AGP's built-in Kotlin support. Only `android-application`, `kotlin-compose`, `hilt`, and `ksp` are applied.
- **KSP versioning:** Kotlin 2.2.x uses KSP `2.2.x-2.0.y` scheme (not `1.0.y`).
- **`android.disallowKotlinSourceSets=false`** in `gradle.properties` is required for KSP + AGP 9.x.
- **Hilt ≥ 2.59.2** required for AGP 9.x compatibility.
- **`AnthropicOkHttpClient`** (not the default client) must be used for minSdk 24 compatibility.
- **Core library desugaring** is enabled (`isCoreLibraryDesugaringEnabled = true`) for OkHttp on minSdk 24.

## Anthropic SDK (0.8.0) Notes

- Import package: `com.anthropic.models.messages`
- `maxTokens` builder method takes `Long`, not `Int`
- Content blocks: use `isText()` / `asText().text()` — there is no `ContentBlock.Text` class
- Optional values use Java `Optional`: `response.stopReason().orElse(null)`
- Model in use: `claude-haiku-4-5-20251001`

## API Key

`CLAUDE_API_KEY` is read from `local.properties` (not committed) and injected via `BuildConfig.CLAUDE_API_KEY`.
 