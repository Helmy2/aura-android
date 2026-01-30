# AGENTS.md - Aura Android

Guidelines for AI coding agents working on this codebase.

## Project Overview

Aura is a wallpaper and video discovery Android app built with Jetpack Compose, using MVI architecture, Navigation3, and Koin for dependency injection.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean assembleDebug
```

## Test Commands

```bash
# Run all unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.example.aura.ExampleUnitTest"

# Run a single test method
./gradlew test --tests "com.example.aura.ExampleUnitTest.addition_isCorrect"

# Run all instrumented tests
./gradlew connectedAndroidTest

# Run screenshot tests
./gradlew validateDebugScreenshotTest

# Update screenshot baselines
./gradlew updateDebugScreenshotTest
```

## Lint & Static Analysis

```bash
# Run Android lint
./gradlew lint

# Generate lint report
./gradlew lintDebug
# Report at: app/build/reports/lint-results-debug.html
```

## Project Structure

```
app/src/main/kotlin/com/example/aura/
├── MainActivity.kt              # Entry point
├── MainApplication.kt           # Koin initialization
├── MainApp.kt                   # Root composable
├── di/AppModule.kt              # Koin module definitions
├── feature/                     # Feature modules
│   ├── home/                    # Home screen
│   ├── wallpaper/list/          # Wallpaper gallery
│   ├── wallpaper/detail/        # Wallpaper detail
│   ├── videos/list/             # Video gallery
│   ├── videos/detail/           # Video player
│   ├── favorites/               # Saved items
│   └── settings/                # Theme settings
└── shared/
    ├── core/mvi/                # MVI framework
    ├── core/util/               # Utilities
    ├── navigation/              # Navigation3 routing
    ├── component/               # Reusable composables
    └── theme/                   # Material3 theming
```

## Code Style Guidelines

### Imports

- Use explicit imports, no wildcard imports
- Order: Android/Androidx → Third-party → Project packages
- Remove unused imports

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.aura.shared.core.mvi.ContainerHost
import org.koin.compose.viewmodel.koinViewModel
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Classes | PascalCase | `WallpaperListViewModel` |
| Functions | camelCase | `onWallpaperClicked` |
| Properties | camelCase | `isLoading` |
| Constants | SCREAMING_SNAKE | `BACK_STACK_KEY` |
| Composables | PascalCase | `WallpaperGallery` |
| State classes | `*State` suffix | `WallpaperListState` |
| Effect classes | `*Effect` suffix | `WallpaperListEffect` |
| ViewModels | `*ViewModel` suffix | `WallpaperListViewModel` |
| Screens | `*Screen` suffix | `WallpaperListScreen` |

### MVI Architecture Pattern

Each feature has 3 files:
1. `*State.kt` - Immutable state data class + sealed Effect interface
2. `*ViewModel.kt` - ContainerHost implementation
3. `*Screen.kt` - Composable UI

**State Definition:**
```kotlin
data class FeatureState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface FeatureEffect {
    data class ShowError(val message: String) : FeatureEffect
}
```

**ViewModel Pattern:**
```kotlin
class FeatureViewModel(
    private val repository: Repository
) : ContainerHost<FeatureState, FeatureEffect>, ViewModel() {

    override val container = container<FeatureState, FeatureEffect>(FeatureState())

    fun onAction() = intent {
        reduce { copy(isLoading = true) }
        try {
            val result = repository.fetch()
            reduce { copy(items = result, isLoading = false) }
        } catch (e: Exception) {
            reduce { copy(isLoading = false) }
            postSideEffect(FeatureEffect.ShowError(e.message ?: "Error"))
        }
    }
}
```

**Screen Pattern:**
```kotlin
@Composable
fun FeatureScreen(viewModel: FeatureViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()

    viewModel.CollectSideEffect { effect ->
        when (effect) {
            is FeatureEffect.ShowError -> { /* show snackbar */ }
        }
    }

    // UI content
}
```

### Navigation (Navigation3)

Routes are defined as serializable sealed interface:
```kotlin
@Serializable
sealed interface Destination : NavKey {
    @Serializable
    data object FeatureList : Destination

    @Serializable
    data class FeatureDetail(val item: Item) : Destination
}
```

Navigate via `AppNavigator`:
```kotlin
navigator.navigate(Destination.FeatureDetail(item))
navigator.back()
```

### Dependency Injection (Koin)

Register in `di/AppModule.kt`:
```kotlin
val appModule = module {
    single { Repository() }
    viewModelOf(::FeatureViewModel)
}
```

### Compose Best Practices

- Use `derivedStateOf` for computed values in composition
- Use `remember` with appropriate keys
- Prefer `Modifier` as first optional parameter
- Use state hoisting - pass state down, events up
- Handle loading/error/empty states explicitly

### Error Handling

- Always catch exceptions in intent blocks
- Log errors with `Log.e(TAG, message, exception)`
- Show user-friendly messages via side effects
- Use `ContainerSettings.exceptionHandler` for global handling

### File Organization

- One public class per file
- State + Effect can share a file (`*State.kt`)
- Keep files under 200 lines when possible

## SDK & Dependencies

| Dependency | Version |
|------------|---------|
| Kotlin | 2.3.0 |
| Compose BOM | 2026.01.00 |
| compileSdk | 36 |
| minSdk | 31 |
| targetSdk | 36 |
| Koin | 4.1.1 |
| Coil | 3.3.0 |
| Media3 | 1.9.1 |
| Navigation3 | 1.0.0 |

## Common Patterns

### Pagination
```kotlin
val shouldLoadMore by remember {
    derivedStateOf {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        lastVisible >= (totalItems - 5) && !isLoading && !isEndReached
    }
}

LaunchedEffect(shouldLoadMore) {
    if (shouldLoadMore) viewModel.onLoadNextPage()
}
```

### Side Effect Collection
```kotlin
viewModel.CollectSideEffect { effect ->
    when (effect) {
        is Effect.ShowError -> snackbarHostState.showSnackbar(effect.message)
    }
}
```

## Known Issues to Fix

1. Enable `isMinifyEnabled = true` in release builds
2. Add `@Immutable` to State data classes
3. Extract hardcoded strings to `strings.xml`
4. Add contentDescription for accessibility
5. Add unit tests for ViewModels
