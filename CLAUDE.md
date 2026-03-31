# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Working Style

 **Fast path:** For clearly small and unambiguous changes
 (single-line fixes, typos, obvious renames), skip steps 1–8
 and proceed directly.

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

## Tech Stack

- **Language:** Kotlin only — no Java source files
- **UI:** Jetpack Compose only — no XML layouts, no `View` subclasses
- **Async:** Kotlin Coroutines + `Flow` / `StateFlow` — no `LiveData`, no RxJava, no raw callbacks
- **DI:** Hilt only — no Koin, no manual Dagger component wiring
- **Database:** Room only — no raw SQLite, no Realm
- **Min SDK:** 24 — core library desugaring is enabled; standard `java.time` APIs are available

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew testDebugUnitTest      # Run unit tests (debug)
./gradlew lint                   # Run lint checks
./gradlew installDebug           # Build and install on connected device/emulator
```

## Modules

| Module | Purpose |
|--------|---------|
| `:domain-models` | Pure Kotlin JVM — shared domain models (`Chat`, `ChatMessage`). No Android dependencies. |
| `:feature-claude` | Self-contained Claude API feature — `ClaudeApiService`, `ClaudeRepository`, `SendMessageUseCase`, `ClaudeModule` (Hilt DI). Reads API key from its own `BuildConfig`. |
| `:database` | Room database — entities, DAOs, `AppDatabase`. |
| `:app` | Presentation layer — screens, ViewModels, UI models, mappers, navigation, `DatabaseModule`. |

Module path constants are declared as `val` at the top of each `build.gradle.kts` (e.g. `val moduleDatabase = ":database"`) — never hardcode module name strings in dependency declarations.

Dependency graph (arrows = "depends on"):
```
:domain-models  (no deps)
      ↑
:feature-claude   :database
      ↑               ↑
           :app
```

## Architecture

Clean Architecture + MVVM across four Gradle modules.

```
:domain-models
  Chat, ChatMessage

:feature-claude
  data/remote/      ClaudeApiService       → Anthropic SDK
  data/repository/  ClaudeRepositoryImpl
  domain/repository/ClaudeRepository
  domain/usecase/   SendMessageUseCase
  di/               ClaudeModule

:database
  entity/   ChatEntity, MessageEntity
  dao/      ChatDao, MessageDao
  AppDatabase

:app
  presentation/
    navigation/   AppNavigation, Screen
    home/         HomeScreen, HomeViewModel, HomeUiState (sealed), HomeUiModel, MessageUiModel
    chats/        ChatsScreen, ChatsViewModel, ChatUiModel
    settings/     SettingsScreen
  domain/
    repository/   ChatRepository
  data/
    local/        ChatRepositoryImpl
  di/             DatabaseModule
```

**Message send flow:**
```
HomeScreen → HomeViewModel.sendMessage()
  → ChatRepository.saveMessage()        // persist user message
  → ChatRepository.updateChatSettings() // persist settings
  → SendMessageUseCase()
      → ClaudeRepository.sendMessage()
          → ClaudeApiService            // Anthropic SDK
  → ChatRepository.saveMessage()        // persist assistant response
```

**Chat load flow (on launch / chat switch):**
```
HomeViewModel.init / loadChat(id)
  → ChatRepository.getLatestChat() / getChatById()
  → currentChatId (MutableStateFlow)
      → flatMapLatest → getMessagesForChat() Flow
          → HomeUiState.Success.messages  // auto-updates from DB writes
          → claudeRepository.countTokens() // updates expectedInputTokens
```

**UI state:**
`HomeViewModel` exposes `StateFlow<HomeUiState>` which is a sealed class:
- `Loading` — initial DB load, chat switch, new chat creation
- `Success` — chat loaded; contains messages, settings, `isSending`, `sendError`, `expectedInputTokens`
- `Error` — fatal failure (DB error, chat not found)

**UI models vs domain models:**
- Domain models (`Chat`, `ChatMessage`) live in `:domain-models`, used across all modules.
- UI models (`MessageUiModel`, `ChatUiModel`) live in `:app` next to their screens, with mapper extension functions (`Chat.toUiModel()`, `ChatMessage.toUiModel()`).
- ViewModels map domain → UI before updating state. `HomeViewModel.sendMessage()` reverse-maps `MessageUiModel → ChatMessage` for the API call.

**Shared ViewModel in navigation:**
`HomeViewModel` is instantiated once in `AppNavigation` (activity-scoped via `hiltViewModel()`), passed explicitly to `HomeScreen`. This allows `ChatsScreen` to trigger `homeViewModel.loadChat(chatId)` before navigating to the Home tab.

**DI:** `ClaudeModule` (in `:feature-claude`) wires `AnthropicOkHttpClient`, `ClaudeApiService`, `ClaudeRepository`, `SendMessageUseCase`. `DatabaseModule` (in `:app`) wires `AppDatabase` → DAOs → `ChatRepository`.

## Established Patterns

Follow these patterns consistently. When adding new screens or features, match the existing structure rather than inventing alternatives.

**UiState sealed class** — every screen has its own `XxxUiState` sealed class with exactly three states:
```kotlin
sealed class XxxUiState {
    data object Loading : XxxUiState()
    data class Success(/* screen data */) : XxxUiState()
    data class Error(val message: String) : XxxUiState()
}
```

**Screen / Content split** — every screen composable is split in two:
- `XxxScreen` — accepts `PaddingValues` + callbacks + `viewModel: XxxViewModel = hiltViewModel()`, collects state, delegates to `XxxContent`
- `XxxContent` — pure composable, no ViewModel, no `hiltViewModel()` call; receives `uiState` and callbacks directly — this is what `@Preview` functions call

**StateFlow conventions:**
- `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue)`
- Collected in UI with `collectAsStateWithLifecycle()` (never `collectAsState()`)
- ViewModel holds only `StateFlow` / `MutableStateFlow`, never `mutableStateOf`

**Preview conventions:**
- Always `private`, always `@Preview(showBackground = true)`
- One preview per meaningful state: `Loading`, `Success` (with data), `Success` (empty/edge case), `Error`
- Named `XxxYyyPreview` (e.g. `ChatsLoadingPreview`, `ChatsSuccessPreview`)
- Call `XxxContent(...)` directly, never `XxxScreen`

**Mapper extension functions** — domain → UI mapping lives as an extension on the domain type, co-located with the UI model file:
```kotlin
fun Chat.toUiModel() = ChatUiModel(id = id, name = name)
```

## Anti-Patterns

Never introduce these — push back if a suggestion involves them:

- `LiveData` or `MutableLiveData` anywhere
- XML layout files or `View`/`ViewGroup` subclasses
- `mutableStateOf` / `remember` inside a `ViewModel`
- Raw `CoroutineScope(...)` inside a `ViewModel` — always use `viewModelScope`
- `collectAsState()` — use `collectAsStateWithLifecycle()` instead
- Hardcoded module name strings in `build.gradle.kts` — always use the `val moduleXxx` constant
- Calling `hiltViewModel()` inside a `XxxContent` composable — only `XxxScreen` may do this

## Key Build Constraints

- **AGP 9.1.0 built-in Kotlin:** Do NOT apply `kotlin-android` plugin in `app/build.gradle.kts` — it conflicts with AGP's built-in Kotlin support. Only `android-application`, `kotlin-compose`, `hilt`, and `ksp` are applied.
- **`:domain-models` uses `kotlin("jvm")` plugin** — no Android SDK dependency. Source sets live under `src/main/kotlin/`.
- **KSP versioning:** Kotlin 2.2.x uses KSP `2.2.x-2.0.y` scheme (not `1.0.y`).
- **`android.disallowKotlinSourceSets=false`** in `gradle.properties` is required for KSP + AGP 9.x.
- **Hilt ≥ 2.59.2** required for AGP 9.x compatibility.
- **`AnthropicOkHttpClient`** (not the default client) must be used for minSdk 24 compatibility.
- **Core library desugaring** is enabled (`isCoreLibraryDesugaringEnabled = true`) in `:app` and `:feature-claude` for minSdk 24 compatibility.

## Anthropic SDK (0.8.0) Notes

- Import package: `com.anthropic.models.messages`
- `maxTokens` builder method takes `Long`, not `Int`
- Content blocks: use `isText()` / `asText().text()` — there is no `ContentBlock.Text` class
- Optional values use Java `Optional`: `response.stopReason().orElse(null)`
- Token usage after response: `response.usage().inputTokens()` / `response.usage().outputTokens()` (both `Long`)
- Pre-flight token count: `client.messages().countTokens(MessageCountTokensParams)` → `MessageTokensCount.inputTokens(): Long`
- Model in use: `claude-haiku-4-5-20251001`

## API Key

`CLAUDE_API_KEY` is read from `local.properties` (not committed) and injected via `BuildConfig.CLAUDE_API_KEY`. Both `:app` and `:feature-claude` read from `local.properties` independently — `:feature-claude` owns the Anthropic client and is self-contained.

## Subagent Workflow

When a task has been approved and is ready for implementation,
use two subagents in sequence.

### Agent 1 — Executor
Model: sonnet  (use model: "sonnet" in the Agent tool call)   (complex implementation, needs full reasoning)
Spawn with the Agent tool. Pass ALL of the following in the task prompt:
- the approved step-by-step plan (copy it verbatim)
- full content of CLAUDE.md (so agent knows all constraints)
- list of files that will be modified

Instruction to agent:
"Implement the following plan strictly as described.
Do not add extra features. Do not modify files outside the plan.
After each step, report what was done and which files were changed."

### Agent 2 — Reviewer
Model: haiku  (use model: "haiku" in the Agent tool call)  (structured checklist, cheaper, faster)
Spawn after Executor completes. Pass:
- the original approved plan (verbatim)
- the full report from Executor (files changed + what was done)
- full content of CLAUDE.md (for Key Build Constraints reference)

Instruction to agent:
"Review the implementation against the plan. Check:
1. Does every step of the plan have a corresponding change?
2. Are Key Build Constraints from CLAUDE.md respected?
3. Run ./gradlew assembleDebug from the project root.
   If it fails:
    - If the error is a trivial fix (typo, missing import, wrong type) —
      fix it directly and re-run the build.
    - If the error requires understanding the original plan or
      touching multiple files — include the full error in your report
      and do NOT attempt to fix.
4. Are there any obvious logic errors or missing edge cases?
   Output: PASS or a numbered list of issues with file:line references."

### After Reviewer completes
- If PASS → report to user and stop.
- If issues found → do NOT spawn another Executor automatically.
  Report the issues to the user and wait for explicit instructions.

### When NOT to use subagents
- Small single-file changes — implement directly
- Refactoring within one module — no subagents needed, implement directly,
  but always run ./gradlew assembleDebug when done.
- Questions and planning phase — main agent only