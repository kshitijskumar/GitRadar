# GitRadar — IntelliJ Plugin Port Plan

## Context

GitRadar is currently a Compose Multiplatform desktop app (`:composeApp` module, JVM/Android/iOS targets) that shows the user two lists of GitHub PRs: PRs they own that are under review, and PRs assigned to them for review. Auth is a manually entered Personal Access Token + repo URL stored in DataStore; PR data is fetched from GitHub REST via Ktor; PR-resolution state is tracked locally in SQLDelight. Architecture is MVVM with manual DI, all clean Kotlin Flows.

The goal is to ship a parallel **IntelliJ Platform plugin** that exposes the same two-tab PR panel inside any IntelliJ-based IDE (IntelliJ IDEA, Android Studio, etc.). The plugin should auto-detect the GitHub repo from the open project's git remote, store the PAT securely in IntelliJ's `PasswordSafe`, poll for updates in the background, and surface IDE notifications when review activity changes. Maximum code reuse from the existing Compose app is expected — UI will use **Jewel (Compose for IDE)** so the existing screens largely lift over.

**Key constraints / decisions agreed with the user:**
- UI toolkit: **Jewel** (Compose-for-IDE) — reuse current Compose composables
- Repo source: **auto-detect from git remote** of the open IntelliJ project
- Repo layout: **new `:intellijPlugin` Gradle module** in this monorepo, plus a new `:shared` module for extracted business logic
- Account model: **single global account, per-project repo** (one PAT stored at application level; each open project resolves its own repo)
- Min IDE target: **2024.2 (build 242)**, JDK 21
- Extras in v1: background polling, IDE balloon notifications, PasswordSafe token storage, browser click-through on PR

---

## Target architecture

```
GitRadar/
├── shared/                       ← NEW. Pure-Kotlin, no Compose, no Android.
│   └── src/commonMain/kotlin/.../
│       ├── data/app/             (AppLocalDataSource, AppRemoteDataSource interfaces + impl)
│       ├── data/github/          (Ktor client factory + models)
│       ├── data/pulls/           (PullRequestsManager)
│       ├── data/model/           (LoggedInUser, GithubRepoRef, …)
│       ├── data/local/db/        (SQLDelight schema + factory interface)
│       └── data/local/prefs/     (CredentialStore + RepoPrefs interfaces — no DataStore here)
│
├── composeApp/                   ← existing desktop / Android / iOS targets
│   └── depends on :shared; provides DataStore-backed CredentialStore impl, JVM SQLite driver, Compose UI
│
└── intellijPlugin/               ← NEW. JVM-only IntelliJ plugin.
    └── src/main/kotlin/.../
        ├── plugin.xml
        ├── services/             (Application + Project services)
        ├── credentials/          (PasswordSafe-backed CredentialStore impl)
        ├── git/                  (Git4Idea-based RepoDetector)
        ├── toolwindow/           (ToolWindowFactory + Jewel ComposePanel)
        ├── notifications/        (NotificationGroupManager wiring + diff logic)
        └── settings/             (Application settings panel for account)
```

The `:shared` module is platform-agnostic Kotlin Multiplatform (no AndroidX, no Compose). `:composeApp` keeps all its current behavior by implementing the small set of `expect`/interface boundaries that previously lived inside it. `:intellijPlugin` provides IDE-flavored implementations of the same boundaries.

---

## Phased plan

Each phase below is self-contained and can be handed to a separate agent. Phases are ordered by dependency. Within a phase, sub-tasks (a/b/c) are usually parallelizable.

---

### Phase 1 — Extract `:shared` module (refactor only, no new features)

**Why first:** The plugin cannot consume the data layer until it's split out of the Compose-app module. Until then, even a stub plugin would have to duplicate code.

**Critical files to touch:**
- New: [shared/build.gradle.kts](shared/build.gradle.kts), [settings.gradle.kts](settings.gradle.kts)
- Move (no behavior change):
  - [composeApp/src/commonMain/kotlin/org/example/project/data/app/](composeApp/src/commonMain/kotlin/org/example/project/data/app/)
  - [composeApp/src/commonMain/kotlin/org/example/project/data/github/](composeApp/src/commonMain/kotlin/org/example/project/data/github/)
  - [composeApp/src/commonMain/kotlin/org/example/project/data/pulls/PullRequestsManager.kt](composeApp/src/commonMain/kotlin/org/example/project/data/pulls/PullRequestsManager.kt)
  - [composeApp/src/commonMain/kotlin/org/example/project/data/model/](composeApp/src/commonMain/kotlin/org/example/project/data/model/)
  - [composeApp/src/commonMain/sqldelight/](composeApp/src/commonMain/sqldelight/)
- Introduce new interfaces in `:shared`:
  - `CredentialStore` — `suspend fun get(key): String?`, `suspend fun put(key, value)`, `suspend fun clear()`
  - `LoggedInUserStore` — observable user state (replaces direct DataStore use)
  - `DatabaseDriverFactory` — already platform-specific via `expect`/`actual`; keep that pattern, just move it
- Refactor [AppLocalDataSourceImpl](composeApp/src/commonMain/kotlin/org/example/project/data/app/AppLocalDataSourceImpl.kt) to depend on `CredentialStore` + `LoggedInUserStore` instead of DataStore directly
- In `:composeApp`, add the DataStore-backed implementations of those two interfaces — preserves desktop/Android/iOS behavior 1:1

**Sub-tasks (parallelizable):**
- 1a. Create the module shell, wire `:shared` into `settings.gradle.kts`, make `:composeApp` depend on it. Verify desktop build still runs.
- 1b. Move data/github/model/pulls packages into `:shared/commonMain`. Update imports across `:composeApp`.
- 1c. Introduce `CredentialStore`/`LoggedInUserStore` interfaces and refactor `AppLocalDataSourceImpl`. Provide DataStore-backed impls in `:composeApp`.
- 1d. Move SQLDelight schema into `:shared`; expose `DatabaseDriverFactory` as `expect`/`actual`.

**Verification:**
- `./gradlew :composeApp:run` still launches and logs in successfully
- `./gradlew :composeApp:check` passes
- Existing PR list still renders and refreshes for a known repo

---

### Phase 2 — Scaffold `:intellijPlugin` module with empty ToolWindow

**Why now:** Get a runnable plugin shell with a tool window stub. Lets every subsequent phase iterate against a real IDE sandbox via `runIde`.

**Critical files (new):**
- `intellijPlugin/build.gradle.kts` — apply `org.jetbrains.intellij.platform` Gradle plugin (v2.x), target `IC-2024.2`, JDK 21, depend on `:shared`, declare Jewel dependency (`org.jetbrains.jewel:jewel-ide-laf-bridge-243` or matching stable), depend on the bundled `org.jetbrains.plugins.git` plugin for Git4Idea.
- `intellijPlugin/src/main/resources/META-INF/plugin.xml` — id `org.example.gitradar`, name, description, `<depends>com.intellij.modules.platform</depends>`, `<depends>Git4Idea</depends>`, register `ToolWindowFactory` on right tool window stripe, icon 13×13.
- `intellijPlugin/src/main/kotlin/org/example/gitradar/toolwindow/GitRadarToolWindowFactory.kt` — for now, render a placeholder Jewel-themed "GitRadar — coming soon" label inside a `ComposePanel`.

**Verification:**
- `./gradlew :intellijPlugin:runIde` launches a sandbox IDE
- Tool window appears on the right stripe with the placeholder
- Plugin loads without errors in the IDE log

---

### Phase 3 — Credentials, settings, and per-project repo detection

**Why now:** Before any UI port, the plugin needs to be able to read/write the PAT and figure out which repo to query.

**Sub-tasks (parallelizable):**

- **3a. PasswordSafe-backed `CredentialStore`** — new
  `intellijPlugin/.../credentials/PasswordSafeCredentialStore.kt` implementing `:shared`'s `CredentialStore`. Uses `PasswordSafe.instance` + `CredentialAttributes("GitRadar:github-token")`. Application-level service.

- **3b. Application settings UI** — new `intellijPlugin/.../settings/GitRadarAppSettingsConfigurable.kt` extending `Configurable`. Two fields: GitHub username + PAT (masked). Hooks PasswordSafe via 3a. Reuses validation logic from existing [LoginViewModel](composeApp/src/commonMain/kotlin/org/example/project/screens/login/LoginViewModel.kt) (now lifted into `:shared`).

- **3c. Git remote detection** — new `intellijPlugin/.../git/RepoDetector.kt`. Uses `GitRepositoryManager.getInstance(project).repositories.firstOrNull()?.remotes` and parses the first `github.com` URL into `GithubRepoRef` (reuse existing parser in `:shared`). Exposed as a `StateFlow<GithubRepoRef?>` from a project-level service `GitRadarProjectService`.

- **3d. Glue:** A `GitRadarApplicationService` exposes account state (logged-in user). `GitRadarProjectService` combines account + detected repo + a per-project `PullRequestsManager` instance. Both use `kotlinx.coroutines` scopes tied to `Disposable`/`Service.coroutineScope`.

**Critical files referenced from `:shared`:**
- [LoggedInUser.kt](composeApp/src/commonMain/kotlin/org/example/project/data/model/LoggedInUser.kt) (will move to `:shared` in Phase 1) — keep the regex parser
- [PullRequestsManager.kt](composeApp/src/commonMain/kotlin/org/example/project/data/pulls/PullRequestsManager.kt) — instantiate per project

**Verification:**
- Enter a PAT via Settings → Tools → GitRadar; verify it persists across IDE restarts and lands in PasswordSafe
- Open a project whose git remote is a GitHub repo; programmatically (debug log) confirm `RepoDetector` resolves the correct `owner/repo`
- Open a non-GitHub project; confirm the service emits `null` and the UI will be able to show an empty state

---

### Phase 4 — Port the dashboard UI with Jewel

**Why now:** All the data plumbing exists; we just need to render it.

**Critical files:**
- New: `intellijPlugin/.../toolwindow/DashboardPanel.kt` — owns the `ComposePanel`, instantiates a Jewel `SwingBridgeTheme`, hosts the existing `DashboardScreen` composable.
- Reuse (now lifted into `:shared` UI bundle, see note below):
  - [DashboardScreen.kt](composeApp/src/commonMain/kotlin/org/example/project/screens/dashboard/DashboardScreen.kt)
  - [DashboardViewModel.kt](composeApp/src/commonMain/kotlin/org/example/project/screens/dashboard/DashboardViewModel.kt) — already pure Compose/ViewModel; works under Jewel
  - PR-card composables, status badges, tab row
- Theme: Jewel auto-bridges to IDE light/dark theme. The custom dark-only palette in [AppTheme.kt](composeApp/src/commonMain/kotlin/org/example/project/data/theme/AppTheme.kt) should be replaced inside the plugin with `JewelTheme.globalColors` so the panel respects the user's IDE theme. The shared `AppTheme` stays as-is for desktop.

**Note on UI sharing:** UI composables are *not* moved to `:shared` because `:shared` is Compose-free. Instead, introduce a small `:sharedUi` Compose Multiplatform module *or* keep the composables in `:composeApp/commonMain` and have `:intellijPlugin` depend on that source set via Gradle (cleaner: extract into `:sharedUi`). Recommend `:sharedUi` for cleanliness; trade-off is one extra module.

**Sub-tasks:**
- 4a. Decide and create `:sharedUi` (Compose Multiplatform, JVM + Android targets only). Move dashboard/login screens into it.
- 4b. Wire `DashboardPanel` to construct `DashboardViewModel` with the per-project `PullRequestsManager` from Phase 3.
- 4c. Replace hardcoded dark palette inside the plugin's hosting composable with Jewel theme tokens; verify both light and dark IDE themes render correctly.
- 4d. Empty-state composable for "no GitHub remote detected" and "not logged in → open Settings".
- 4e. Wire PR-card click → `BrowserUtil.browse(htmlUrl)`.

**Verification:**
- `runIde`, log in via settings, open a GitHub-repo project → dashboard renders both tabs
- Toggle IDE theme light/dark → panel follows
- Click a PR → opens in default browser
- Refresh button works; tab switching works

---

### Phase 5 — Background polling + IDE notifications

**Why now:** Core feature parity is done; this is the IDE-native value-add the user explicitly asked for.

**Critical files (new):**
- `intellijPlugin/.../polling/PullRequestPoller.kt` — loops `delay(intervalMs); manager.refresh()` inside `GitRadarProjectService.coroutineScope`. Default interval 5 min; expose as a setting later.
- `intellijPlugin/.../notifications/PullRequestNotifier.kt` — diffs successive PR snapshots from `PullRequestsManager`:
  - New PR assigned to me for review → `NotificationType.INFORMATION` balloon with "Open in browser" action
  - Existing PR's status flips to `ACTION_REQUIRED` → balloon
  - Use `NotificationGroupManager.getInstance().getNotificationGroup("GitRadar")` — register the group in `plugin.xml` as `<notificationGroup id="GitRadar" displayType="BALLOON"/>`
- `plugin.xml` — register a `postStartupActivity` to kick off the poller per project.

**Sub-tasks:**
- 5a. Implement poller with cancellation on project close (`Disposable` chain).
- 5b. Implement diff-based notifier; ensure first emission after IDE start doesn't fire a notification for every existing PR (snapshot baseline on first tick).
- 5c. Wire balloon actions: "Open PR" (BrowserUtil), "Mark resolved" (calls existing `PullRequestsManager.markResolved`).
- 5d. Add a Tools menu action "GitRadar: Refresh now" for manual trigger.

**Verification:**
- Create a test PR in a known repo; within the polling interval the panel updates and a balloon appears
- Close the project → poller stops (verify via debug log, no orphan coroutines)
- Mark a PR resolved from the balloon → status reflects in the panel and persists across restart (SQLDelight)

---

### Phase 6 — Polish, error handling, QA

**Critical files (touch existing):**
- All Phase 3–5 services: add proper error surfaces (rate-limit, 401 invalid token, network failure) → `Notifications` with action "Open Settings".
- `plugin.xml`: real icons (13×13 tool window, 16×16 settings), description, vendor block, change-notes.
- Settings: polling interval slider, "Reset PR resolution cache" button, "Sign out" button.

**Sub-tasks:**
- 6a. Centralized `GithubApiErrorMapper` in `:shared`; route into IDE balloon in `:intellijPlugin`.
- 6b. Audit thread/coroutine usage — all UI updates on EDT via `ApplicationManager.getApplication().invokeLater` or Compose's dispatcher; all IO off-EDT.
- 6c. Manual QA matrix: IntelliJ IDEA 2024.2 + 2025.1, Android Studio Koala+, light/dark, repo with no remotes, non-GitHub remote, expired PAT, rate-limited account.
- 6d. Plugin verifier: `./gradlew :intellijPlugin:verifyPlugin` clean.

**Verification:**
- All error paths surface a useful balloon and don't crash the IDE
- Plugin verifier passes for the declared IDE range
- Plugin install/uninstall in a fresh sandbox leaves no stale state

---

### Phase 7 — Packaging and (optional) marketplace submission

**Sub-tasks:**
- 7a. `./gradlew :intellijPlugin:buildPlugin` → produces a distributable ZIP under `intellijPlugin/build/distributions/`.
- 7b. Document local install ("Install Plugin from Disk…") in `README.md`.
- 7c. (Optional) JetBrains Marketplace: register vendor, fill listing, upload ZIP, await review. Out of scope for the build phase; flag as a follow-up task.

---

## Critical files reference (quick index)

**To extract into `:shared` (Phase 1):**
- [composeApp/src/commonMain/kotlin/org/example/project/data/app/AppLocalDataSourceImpl.kt](composeApp/src/commonMain/kotlin/org/example/project/data/app/AppLocalDataSourceImpl.kt)
- [composeApp/src/commonMain/kotlin/org/example/project/data/app/AppRemoteDataSourceImpl.kt](composeApp/src/commonMain/kotlin/org/example/project/data/app/AppRemoteDataSourceImpl.kt)
- [composeApp/src/commonMain/kotlin/org/example/project/data/github/GithubHttpClientFactory.kt](composeApp/src/commonMain/kotlin/org/example/project/data/github/GithubHttpClientFactory.kt)
- [composeApp/src/commonMain/kotlin/org/example/project/data/pulls/PullRequestsManager.kt](composeApp/src/commonMain/kotlin/org/example/project/data/pulls/PullRequestsManager.kt)
- [composeApp/src/commonMain/kotlin/org/example/project/data/model/LoggedInUser.kt](composeApp/src/commonMain/kotlin/org/example/project/data/model/LoggedInUser.kt)
- [composeApp/src/commonMain/sqldelight/](composeApp/src/commonMain/sqldelight/)

**To extract into `:sharedUi` (Phase 4):**
- [composeApp/src/commonMain/kotlin/org/example/project/screens/dashboard/](composeApp/src/commonMain/kotlin/org/example/project/screens/dashboard/)
- [composeApp/src/commonMain/kotlin/org/example/project/screens/login/](composeApp/src/commonMain/kotlin/org/example/project/screens/login/)

**Plugin entry surface:**
- `intellijPlugin/src/main/resources/META-INF/plugin.xml`
- `intellijPlugin/src/main/kotlin/org/example/gitradar/toolwindow/GitRadarToolWindowFactory.kt`
- `intellijPlugin/src/main/kotlin/org/example/gitradar/services/GitRadarApplicationService.kt`
- `intellijPlugin/src/main/kotlin/org/example/gitradar/services/GitRadarProjectService.kt`

---

## End-to-end verification (after all phases)

1. Fresh IntelliJ sandbox via `./gradlew :intellijPlugin:runIde`
2. Open Settings → Tools → GitRadar; enter GitHub username + PAT; save
3. Open a project pointing at a known GitHub repo with active PRs
4. Tool window on the right shows two tabs populated with the same data the desktop app shows
5. Toggle IDE theme; UI follows
6. Wait one polling interval after a new review request is created on GitHub → IDE balloon appears; click "Open" → browser opens the PR
7. Mark a PR resolved → status updates in panel; restart IDE → status persists
8. Open a second project with a different GitHub repo → its tool window shows that repo's PRs (per-project), but the PAT is the same (global account)
9. Open a project with no GitHub remote → empty-state UI with helpful message
10. `./gradlew :composeApp:run` — desktop app still works unchanged, sharing the same `:shared` data layer
