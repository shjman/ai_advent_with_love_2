# CLAUDE.md

## Working Style

**Fast path:** Single-line fixes, typos, obvious renames — skip planning and proceed directly.

Otherwise, before coding: ask until requirements are unambiguous, present a step-by-step plan, wait for explicit approval. Prefer short question rounds over starting with incomplete information. Surface alternatives and disagreements rather than choosing silently.

During: check in at unexpected findings or decision points. After: run all three checks before reporting done:
```bash
./gradlew detekt        # Kotlin static analysis — must pass with 0 issues
./gradlew lintDebug     # Android Lint — must pass with 0 errors
./gradlew assembleDebug # Compilation — must succeed
```
Fix all errors. For warnings: fix if straightforward; report if it requires a design decision.

## Tech Stack

Kotlin · Jetpack Compose · Coroutines + StateFlow · Hilt · Room · minSdk 24 (desugaring enabled)

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew testDebugUnitTest      # Run unit tests (debug)
./gradlew lintDebug              # Android Lint (all modules via checkDependencies)
./gradlew detekt                 # Kotlin static analysis (all modules, root task)
./gradlew installDebug           # Build and install on connected device/emulator
```

## Modules

| Module | Purpose |
|--------|---------|
| `:domain-models` | Pure Kotlin JVM — `Chat`, `ChatMessage`. No Android deps. |
| `:feature-claude` | Claude API — `ClaudeApiService`, `ClaudeRepository`, `SendMessageUseCase`, Hilt DI. |
| `:database` | Room — entities, DAOs, `AppDatabase`. |
| `:app` | Presentation — screens, ViewModels, UI models, navigation, `DatabaseModule`. |

Module path constants: declared as `val moduleXxx` at the top of each `build.gradle.kts` — never hardcode strings.

Dependency graph: `:app` → `:feature-claude`, `:database` → `:domain-models`

## Architecture

Clean Architecture + MVVM.

**Message send flow:**
`HomeScreen → HomeViewModel.sendMessage() → ChatRepository.saveMessage() → updateChatSettings() → SendMessageUseCase → ClaudeRepository → ClaudeApiService → ChatRepository.saveMessage()`

**Chat load flow:**
`HomeViewModel.init/loadChat() → ChatRepository → currentChatId (MutableStateFlow) → flatMapLatest → getMessagesForChat() Flow → HomeUiState.Success.messages + countTokens()`

**UI state:** `StateFlow<XxxUiState>` — `Loading` (initial/switch/new), `Success` (loaded), `Error` (fatal).

**UI models:** Domain models in `:domain-models`. UI models in `:app` next to their screens, with `XxxDomain.toUiModel()` mappers. ViewModels map domain → UI before emitting state.

**Shared ViewModel:** `HomeViewModel` is activity-scoped in `AppNavigation` (`hiltViewModel()`), passed explicitly to `HomeScreen`. `ChatsScreen` calls `homeViewModel.loadChat(id)` before navigating to Home.

## Established Patterns

**UiState** — every screen has `XxxUiState` sealed class: `Loading` / `Success(data)` / `Error(message: String)`. See `HomeUiState.kt`, `ChatsUiState.kt`.

**Screen/Content split:**
- `XxxScreen` — holds VM (`hiltViewModel()`), collects state, delegates to `XxxContent`
- `XxxContent` — pure composable, no VM; receives state + callbacks — used by `@Preview`

**StateFlow:** `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue)`. Collect with `collectAsStateWithLifecycle()`.

**Previews:** `private`, `@Preview(showBackground = true)`, one per state (Loading / Success / empty / Error), named `XxxYyyPreview`, always call `XxxContent`.

**Mappers:** extension on domain type, co-located with UI model file: `fun Chat.toUiModel() = ...`

## Constraints

**Build:**
- Do NOT apply `kotlin-android` plugin in `app/build.gradle.kts` — conflicts with AGP 9.1.0 built-in Kotlin. Only: `android-application`, `kotlin-compose`, `hilt`, `ksp`.
- `:domain-models` uses `kotlin("jvm")`, sources under `src/main/kotlin/`.
- KSP: Kotlin 2.2.x → KSP `2.2.x-2.0.y` (not `1.0.y`).
- `android.disallowKotlinSourceSets=false` in `gradle.properties` required for KSP + AGP 9.x.
- Hilt ≥ 2.59.2 for AGP 9.x.
- Use `AnthropicOkHttpClient` (not default) for minSdk 24.

**Never use:**
- `LiveData` / `MutableLiveData`
- XML layouts or `View` subclasses
- `mutableStateOf` in ViewModel — use `MutableStateFlow`
- Raw `CoroutineScope` in ViewModel — use `viewModelScope`
- `collectAsState()` — use `collectAsStateWithLifecycle()`
- Hardcoded module name strings in `build.gradle.kts`
- `hiltViewModel()` inside `XxxContent` — only in `XxxScreen`

## Anthropic SDK (0.8.0)

- Import: `com.anthropic.models.messages`
- `maxTokens` takes `Long`
- Content blocks: `isText()` / `asText().text()` — no `ContentBlock.Text` class
- Optional values: Java `Optional` — `response.stopReason().orElse(null)`
- Token usage: `response.usage().inputTokens()` / `.outputTokens()` (both `Long`)
- Pre-flight count: `client.messages().countTokens(MessageCountTokensParams)` → `MessageTokensCount.inputTokens(): Long`
- Model: `claude-haiku-4-5-20251001`

## API Key

`CLAUDE_API_KEY` from `local.properties` → `BuildConfig.CLAUDE_API_KEY`. Both `:app` and `:feature-claude` read it independently.

## Subagent Workflow

Use for approved, multi-file tasks. Skip for single-file changes, single-module refactors, or planning.

### Agent 1 — Executor (model: sonnet)
Pass: approved plan (verbatim) + CLAUDE.md content + files to modify.
Instruction: "Implement the plan strictly. No extra features. No files outside the plan. Report each step and files changed."

### Agent 2 — Reviewer (model: haiku)
Pass: approved plan + Executor report + CLAUDE.md content.
Instruction: "Check: (1) every plan step has a change, (2) Constraints from CLAUDE.md respected, (3) run `./gradlew assembleDebug` — fix trivial errors (typo/import/type) directly, report complex ones, (4) logic errors or missing edge cases. Output: PASS or numbered issues with file:line."

After Reviewer: PASS → report and stop. Issues → report to user, wait for instructions. Never auto-spawn another Executor.
