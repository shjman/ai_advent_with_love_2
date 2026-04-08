---
name: skill-new-screen
description: Playbook for creating a new screen in ai_advent_with_love_2. Use when the plan includes adding a new navigation destination, bottom sheet, or dialog with its own ViewModel.
triggers: [new screen, new tab, new destination, new bottom sheet, new dialog with state]
---

# Purpose
Create a new screen following the established Screen/View/UiState pattern of the project.
This skill ensures consistency with existing screens (HomeScreen, ChatsScreen).

# When to use
- Plan step says "create XxxScreen" or "add new screen"
- A new bottom navigation tab is needed
- A new full-screen destination is added to NavHost
- Do NOT use for simple composables that don't need their own ViewModel

# Inputs needed
- Feature name in PascalCase (e.g. `Stats`, `Profile`)
- Navigation route string
- What data the screen needs to display
- Whether it needs a ViewModel or is purely static

# File layout
Every screen produces exactly these files (one class per file — no exceptions):

```
presentation/<featureName>/
    <FeatureName>UiState.kt      ← sealed state class
    <FeatureName>ViewModel.kt    ← HiltViewModel
    <FeatureName>Screen.kt       ← thin adapter (hiltViewModel lives here)
    <FeatureName>View.kt         ← pure layout (no ViewModel, no remember, no previews)
    <FeatureName>Preview.kt      ← all @Preview functions
```

---

## Step 1 — Create UiState
File: `app/src/main/java/.../presentation/<featureName>/<FeatureName>UiState.kt`

```kotlin
package com.yahorshymanchyk.ai_advent_with_love_2.presentation.<featureName>

sealed class <FeatureName>UiState {
    data object Loading : <FeatureName>UiState()
    data class Success(
        // all data the UI needs — no separate StateFlows
    ) : <FeatureName>UiState()
    data class Error(val message: String) : <FeatureName>UiState()
}
```

Rules:
- Always three states: Loading, Success, Error
- `data object Loading` — no data, so no fields
- `data class Success` — holds ALL data the View needs
- `data class Error(val message: String)` — human-readable message

---

## Step 2 — Create ViewModel
File: `app/src/main/java/.../presentation/<featureName>/<FeatureName>ViewModel.kt`

```kotlin
package com.yahorshymanchyk.ai_advent_with_love_2.presentation.<featureName>

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class <FeatureName>ViewModel @Inject constructor(
    // inject use cases only — never repositories directly
) : ViewModel() {

    private val _uiState = MutableStateFlow<<FeatureName>UiState>(<FeatureName>UiState.Loading)
    val uiState: StateFlow<<FeatureName>UiState> = _uiState.asStateFlow()
}
```

Rules:
- `@HiltViewModel` + `@Inject constructor` always
- ONE `uiState: StateFlow<XxxUiState>` — never multiple separate StateFlows for screen state
- Use `viewModelScope` — never raw `CoroutineScope`
- Never `mutableStateOf` — always `MutableStateFlow`
- Never `LiveData`
- Inject use cases, not repositories

For Flow-based data (e.g. Room), use `stateIn`:
```kotlin
val uiState: StateFlow<XxxUiState> = someUseCase()
    .map<_, XxxUiState> { data -> XxxUiState.Success(data) }
    .catch { emit(XxxUiState.Error(it.message ?: "Unknown error")) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = XxxUiState.Loading
    )
```

---

## Step 3 — Create Screen
File: `app/src/main/java/.../presentation/<featureName>/<FeatureName>Screen.kt`

```kotlin
package com.yahorshymanchyk.ai_advent_with_love_2.presentation.<featureName>

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun <FeatureName>Screen(
    paddingValues: PaddingValues,
    viewModel: <FeatureName>ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    <FeatureName>View(
        uiState = uiState,
        paddingValues = paddingValues,
        onSomeAction = viewModel::someMethod
    )
}
```

Rules:
- `hiltViewModel()` ONLY here — never in View
- `collectAsStateWithLifecycle()` — never `collectAsState()`
- Screen is a thin adapter — no logic, no UI elements
- Pass `paddingValues` received from NavHost through to View
- Pass individual callbacks, not the whole ViewModel

---

## Step 4 — Create View
File: `app/src/main/java/.../presentation/<featureName>/<FeatureName>View.kt`

```kotlin
package com.yahorshymanchyk.ai_advent_with_love_2.presentation.<featureName>

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun <FeatureName>View(
    uiState: <FeatureName>UiState,
    paddingValues: PaddingValues,
    onSomeAction: () -> Unit
) {
    when (uiState) {
        is <FeatureName>UiState.Loading -> Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        is <FeatureName>UiState.Success -> Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            // success UI using uiState.someField
        }

        is <FeatureName>UiState.Error -> Box(
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
```

Rules:
- No ViewModel, no `hiltViewModel()`
- No `remember` — all state comes from `uiState`
- No side-effects (`LaunchedEffect`, `DisposableEffect`) in View body
- No `@Preview` functions — those go in `<FeatureName>Preview.kt`
- Always handle ALL UiState variants via `when` (exhaustive)
- Apply `paddingValues` via `.padding(paddingValues)` on each root container

---

## Step 5 — Create Preview file
File: `app/src/main/java/.../presentation/<featureName>/<FeatureName>Preview.kt`

```kotlin
package com.yahorshymanchyk.ai_advent_with_love_2.presentation.<featureName>

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
private fun <FeatureName>LoadingPreview() {
    <FeatureName>View(
        uiState = <FeatureName>UiState.Loading,
        paddingValues = PaddingValues(),
        onSomeAction = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun <FeatureName>SuccessPreview() {
    <FeatureName>View(
        uiState = <FeatureName>UiState.Success(/* sample data */),
        paddingValues = PaddingValues(),
        onSomeAction = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun <FeatureName>ErrorPreview() {
    <FeatureName>View(
        uiState = <FeatureName>UiState.Error("Something went wrong"),
        paddingValues = PaddingValues(),
        onSomeAction = {}
    )
}
```

Rules:
- `private` always
- One preview per UiState variant minimum
- Always call View, never Screen
- Pass `PaddingValues()` (empty) so layout renders correctly in preview
- Name pattern: `<FeatureName><State>Preview`

---

## Step 6 — Add to Navigation
File: `app/src/main/java/.../presentation/navigation/Screen.kt`

Add the route object if it's a new destination:
```kotlin
data object <FeatureName> : Screen("<featureName>")
```

File: `app/src/main/java/.../presentation/navigation/AppNavigation.kt`

Add composable to NavHost inside the `Scaffold` content lambda:
```kotlin
composable(Screen.<FeatureName>.route) {
    <FeatureName>Screen(paddingValues = innerPadding)
}
```

If it's a bottom navigation tab, also add it to the `tabs` list in `AppNavigation.kt`.

---

## Step 7 — Add Hilt module if needed
Only required if the new screen introduces dependencies that aren't already bound.

If use cases use `@Inject constructor` and their repository dependencies are already provided — no new module needed.

If a new repository or data source binding is required, add to the relevant existing module (`DatabaseModule.kt`, `ClaudeModule.kt`) or create `<FeatureName>Module.kt`.

---

# Quality bar (self-check before finishing)

- [ ] UiState has Loading, Success, Error in its own file
- [ ] ViewModel has single `uiState: StateFlow<XxxUiState>`, no multiple flows
- [ ] Screen only contains `hiltViewModel()`, `collectAsStateWithLifecycle()`, and the View call
- [ ] Screen passes `paddingValues` to View
- [ ] View has no ViewModel reference, no `hiltViewModel()`
- [ ] View has no `remember` calls
- [ ] View has no `@Preview` functions
- [ ] View handles all UiState variants via `when`
- [ ] Previews are in a separate `<FeatureName>Preview.kt` file
- [ ] Previews are `private` and call View (not Screen)
- [ ] Previews pass `PaddingValues()` for padding
- [ ] Navigation route added to NavHost with correct `innerPadding` forwarding
- [ ] No `LiveData` anywhere
- [ ] No `collectAsState()` — only `collectAsStateWithLifecycle()`
- [ ] No `mutableStateOf` in ViewModel

---

# Anti-patterns

- ❌ `hiltViewModel()` inside View or any composable called from View
- ❌ `collectAsState()` instead of `collectAsStateWithLifecycle()`
- ❌ Multiple `StateFlow` fields in ViewModel instead of one `UiState`
- ❌ Business logic in Screen or View
- ❌ Missing Error state in UiState
- ❌ `remember { mutableStateOf(...) }` for data that should come from ViewModel
- ❌ `@Preview` functions inside View file
- ❌ Previews calling Screen instead of View
- ❌ `modifier: Modifier = Modifier` on View — use `paddingValues: PaddingValues` instead
- ❌ Forgetting `.padding(paddingValues)` on root containers in View
