# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android Client for MifosX - a Kotlin Multiplatform (KMP) application for field officers to manage financial services. Supports Android, iOS, and Desktop with offline capability.

**Demo**: `gsoc.mifos.community` (mifos/password)

## Build Commands

```bash
# Full pre-push check (run before PRs)
./ci-prepush.sh

# Individual tasks
./gradlew check -p build-logic          # Verify build-logic
./gradlew spotlessApply --no-configuration-cache  # Format code
./gradlew dependencyGuardBaseline       # Update dependency baseline
./gradlew detekt                        # Static analysis
./gradlew build                         # Full build

# Platform-specific
./gradlew :cmp-android:assembleDebug    # Android debug APK
./gradlew :cmp-desktop:run              # Run desktop app

# Testing
./gradlew testDebug                     # Unit tests
./gradlew :lint:test :mifosng-android:lintRelease  # Lint checks
```

## Architecture

### Module Structure
```
core/           - Shared layers (common, model, domain, data, database, network, datastore, designsystem, ui)
feature/        - Feature modules (auth, client, loan, savings, groups, center, offline, etc.)
cmp-android/    - Android app entry point
cmp-desktop/    - Desktop JVM app
cmp-ios/        - iOS app
cmp-shared/     - Shared composition root
cmp-navigation/ - Navigation and DI aggregation
build-logic/    - Gradle convention plugins
```

### Clean Architecture Layers
1. **Presentation** (feature modules): Compose screens + ViewModels with StateFlow
2. **Domain** (core/domain): Use cases and repository interfaces
3. **Data** (core/data): Repository implementations
4. **Database** (core/database): Room with KMP expect/actual pattern
5. **Network** (core/network): Ktor + Ktorfit for API calls

### Key Patterns
- **State**: Sealed classes for UI state (e.g., `ClientListUiState`)
- **DI**: Koin modules in each module's `di/` package, aggregated in `cmp-navigation/di/KoinModules.kt`
- **Data Flow**: `Flow<DataState<T>>` or `Flow<PagingData<T>>` from repositories

### Adding a New Feature
1. Create module under `feature/[name]/`
2. Apply plugin: `alias(libs.plugins.mifos.cmp.feature)`
3. Structure: Screen.kt, ViewModel.kt, UiState.kt, Route.kt + `di/Module.kt`
4. Register DI module in `cmp-navigation/di/KoinModules.kt`
5. Add navigation route in cmp-navigation

## Key Technologies
- Kotlin 2.1.0, Compose Multiplatform 1.9.0
- Room 2.7.0-rc02 (multiplatform), Ktor 3.1.3, Ktorfit 2.5.2
- Koin 4.0.1-RC1, Paging 3, Coil 3.2.0
- Versions in `gradle/libs.versions.toml`

## Commit Convention
Format: `<type>(<scope>): <subject>`

Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `misc`

Example: `feat(client): add client filter functionality`

## Branch Policy
- Development: `development` branch
- PRs target: `master` branch
- Always pull from `development` for latest changes
