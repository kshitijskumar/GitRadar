# Agents.md

This file captures **project conventions** for UI + ViewModel work so assistants/tools follow the same patterns.

## Screens package conventions

- **All UI + ViewModel related code lives under** `composeApp/src/commonMain/kotlin/org/example/project/screens/`.
- **Feature grouping**: `org.example.project.screens.<feature>` (example: `org.example.project.screens.app`).

## Contract + ViewModel split

- **State/types go in** `*Contract.kt`
  - Example: `AppManagerContract.kt`
  - Contains:
    - `data class <Feature>State(...)`
    - `enum class <Feature>ScreenType` (if needed)
    - `sealed class <Feature>DialogType` (if needed)
- **Logic goes in** `*ViewModel.kt`
  - Example: `AppManagerViewModel.kt`
  - Contains:
    - a single source of truth: private `MutableStateFlow(<Feature>State())`
    - a single public surface: `val state: StateFlow<<Feature>State>`
    - public intent/action functions (e.g. `acknowledgeSessionClearance()`)

## State rules

- **Only one public observable**: `state: StateFlow<State>`
  - No other public vars like `screenType`, `errorMessage`, etc.
  - Those belong inside the `State` object.
- **Defaults must be safe**:
  - nullable fields should default to `null`
  - include `isLoading` when initial state is not fully resolved

## App-level navigation rule (AppManager)

- `screenType` is derived from locally stored user (`LoggedInUser?`):
  - `null` user ⇒ **LOGIN**
  - non-null user ⇒ **DASHBOARD**

## Dialog-first transitions (avoid abrupt dashboard → login)

When the app is on **DASHBOARD** and the stored user becomes `null` (session cleared/expired):

- **Do not immediately switch** `screenType` to LOGIN
- Instead:
  - set `dialogType = SessionClearedDialog` (or similar) in state
  - keep `screenType` unchanged until the user acknowledges
- On user acknowledgement:
  - clear `dialogType`
  - then set `screenType = LOGIN`

## PlatformContext + DataStore usage

- App-local persistence is backed by multiplatform DataStore (Preferences).
- `App()` must receive a `PlatformContext` from platform entrypoints (Android/JVM/iOS) so `createUserDataStore(platformContext)` can work.

