# Aura

A modern Android wallpaper and video discovery application built with Jetpack Compose.

## Overview

Aura allows users to browse curated wallpapers and popular videos, search for specific content, save favorites, and download media to their device. The app features adaptive layouts for different screen sizes and a customizable theme system.

## Features

- **Wallpaper Gallery** - Browse and search high-quality wallpapers in a staggered grid layout
- **Video Gallery** - Discover and stream videos with ExoPlayer integration
- **Favorites** - Save wallpapers and videos for quick access
- **Downloads** - Save media directly to device storage
- **Theme Support** - Light, Dark, and System-default themes
- **Adaptive UI** - Responsive layouts for phones and tablets

## Tech Stack

| Category             | Technology                       |
|----------------------|----------------------------------|
| Language             | Kotlin 2.3.0                     |
| UI Framework         | Jetpack Compose (BOM 2026.01.00) |
| Design System        | Material3                        |
| Architecture         | MVI (Model-View-Intent)          |
| Navigation           | AndroidX Navigation3             |
| Dependency Injection | Koin 4.1.1                       |
| Image Loading        | Coil 3.3.0                       |
| Video Playback       | Media3 ExoPlayer 1.9.1           |
| Serialization        | Kotlinx Serialization            |

## Requirements

- Android Studio Ladybug or newer
- JDK 11+
- Android SDK 36
- Minimum SDK: 31 (Android 12)

## Project Structure

```
app/src/main/kotlin/com/example/aura/
├── MainActivity.kt           # Entry point
├── MainApplication.kt        # Application initialization
├── MainApp.kt                # Root composable
├── di/                       # Koin dependency modules
├── feature/
│   ├── home/                 # Home screen
│   ├── wallpaper/            # Wallpaper list and detail
│   ├── videos/               # Video list and detail
│   ├── favorites/            # Saved content
│   └── settings/             # App preferences
└── shared/
    ├── core/                 # MVI framework, utilities
    ├── navigation/           # Navigation3 routing
    ├── component/            # Reusable UI components
    └── theme/                # Material3 theming
```

## Architecture

The app follows the **MVI (Model-View-Intent)** pattern with a custom implementation:

- **State** - Immutable UI state held in ViewModel
- **Intent** - User actions processed through `intent { }` blocks
- **Side Effects** - One-time events like snackbars and navigation

Navigation uses **AndroidX Navigation3** with type-safe routes via Kotlin Serialization.

## Setup

### GitHub Packages Authentication

The app depends on a shared library hosted on GitHub Packages. Configure authentication in `~/.gradle/gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

Or set environment variables:
```bash
export GITHUB_ACTOR=your_username
export GITHUB_TOKEN=your_token
```

### Build

```bash
./gradlew assembleDebug
```

### Run Tests

```bash
./gradlew test
```

## Testing

### Unit Testing
The project includes unit tests for various components using Kotlin's standard testing tools and Mockito/MockK. Coverage includes:
- **ViewModels**: Logic and state management for all features.
- **Navigation**: Route handling and navigation state.
- **Utilities**: Extension functions and helper classes.
- **Data Layer**: Download management and background tasks.

To run all unit tests:
```bash
./gradlew test
```

### Screenshot Testing
Aura uses the native Android Compose Screenshot Testing tool. The screens are designed for testability using stateless `Content` composables, enabling validation of various UI states including:
- **Loading/Skeleton** states
- **Empty/No Results** views
- **Error/Message** states
- **Pagination Loading**
- **Theme Modes** (System and Light)

#### Run Screenshot Tests
To validate the current UI against stored reference images:
```bash
./gradlew validateDebugScreenshotTest
```

#### Update Reference Images
To generate or update reference images after intentional UI changes:
```bash
./gradlew updateDebugScreenshotTest
```

> [!NOTE]
> All screenshot tests focus on **Light Mode** and **System Theme** default states to ensure core UI stability.

## License

This project is proprietary software.
