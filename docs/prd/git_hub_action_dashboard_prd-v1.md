# GitHub Action Dashboard (macOS)

## 1. Overview

### Problem Statement
Developers frequently keep multiple GitHub tabs open to:
- Review PRs assigned to them
- Track issues assigned to them
- Monitor their own PRs for review feedback or approval status

This leads to:
- Context switching
- Tab clutter
- Manual and repetitive checks
- Missed updates (new comments, commits after review)

### Proposed Solution
A lightweight **macOS application with a status bar companion** that aggregates GitHub activity into a single, action-focused dashboard.

The app presents only what matters:
- What needs action
- What is approved
- What is still a draft

No noise, no replacement of GitHub—just a productivity layer on top.

---

## 2. Goals & Non-Goals

### Goals
- Reduce cognitive load and tab chaos
- Provide a single place to check GitHub responsibilities
- Clearly indicate when user action is required
- Be fast to open, fast to scan

### Non-Goals (V1)
- Writing or submitting reviews
- Commenting on PRs or issues
- Replacing GitHub notifications
- Background polling or webhooks
- Multi-repo or org-wide dashboards

---

## 3. Target Platform

- **macOS only (V1)**
- Native macOS desktop application
- Native macOS **status bar (menu bar) app**

---

## 4. Authentication & Repository Scope

### Authentication
- GitHub **Personal Access Token (PAT)**
- Read-only access

### Repository Scope
- **Single repository only (V1)**
- User provides:
  - Repository URL (e.g. `org/repo`)
  - Personal Access Token

---

## 5. Core UI Structure

### Desktop App

- Main window with **3 tabs**:
  1. **My PRs**
  2. **PRs to Review**
  3. **Issues Assigned**

- Global **Refresh** button
- Fresh data fetch when app opens

### Status Bar App

- Static icon in macOS status bar
- On click:
  - Popup opens
  - Popup UI **matches the desktop app’s 3-tab layout**

> No counts, badges, or indicators on the status bar icon in V1.

---

## 6. Indicator System (Visual State)

Each PR item may show **one indicator** at most.

| State | Indicator | Meaning |
|------|-----------|---------|
| Draft PR | Grey (⚪) | PR is in draft state |
| Approved | Green (🟢) | PR has at least one approval |
| Action Required | Red (🔴) | User needs to take action |
| None | No icon | Informational only |

Indicator priority order:
1. Draft
2. Approved
3. Action Required
4. None

---

## 7. Tab-Specific Product Logic

### 7.1 My PRs

PRs authored by the user.

Evaluation logic (in order):

1. **Draft PR**
   - Show Grey indicator
   - Skip all other checks

2. **Approved PR**
   - If *any* approval exists (with or without comments)
   - Show Green indicator

3. **Action Required**
   - Find latest comment **not authored by the user**
   - Includes:
     - Review comments
     - PR conversation comments
   - If the latest comment is authored by the user, it is considered that the user has already taken action and **no action is required**.
   - If:
     ```
     latest_non_mine_comment_timestamp > latest_commit_timestamp
     ```
     → Show Red indicator

4. **Else**
   - No indicator

---

### 7.2 PRs Assigned to Me for Review

PRs where the user is requested as a reviewer.

Evaluation logic (in order):

1. **Draft PR**
   - Show Grey indicator

2. **Approved by User**
   - If user has approved the PR
   - Show Green indicator

3. **Action Required (Re-review needed)**
   - Find user’s **latest review timestamp**
   - If:
     ```
     latest_commit_timestamp > my_latest_review_timestamp
     ```
     → Show Red indicator

4. **Else**
   - No indicator

---

### 7.3 Issues Assigned to Me

- Issues where the user is assigned
- **No indicators in V1**
- Purely informational

---

## 8. Interaction Behavior

- Clicking any PR or issue:
  - Opens the corresponding GitHub URL
  - Uses the system default browser

Works identically from:
- Desktop app
- Status bar popup

---

## 9. Data Fetching & Refresh Strategy

- Fresh fetch on app launch
- Manual refresh via Refresh button
- Refresh behavior:
  1. Refresh currently visible tab first
  2. Refresh other tabs afterward

No background polling in V1.

---

## 10. API & Data Requirements

API and data-fetching specifics will be finalized during the implementation phase.

---

## 11. Error & Edge Case Handling (V1)

- Invalid / expired token
- Repository access denied
- API failure

In all the above cases:
- The user is redirected to the **login/configuration screen**
- User can re-enter:
  - Repository URL
  - Personal Access Token

Empty states:
- No PRs
- No reviews
- No issues

---

## 12. Development Priorities (Build Order)

### Phase 1 – Foundation
1. Login / configuration screen (repo URL + token)
2. Secure storage of token
3. GitHub GraphQL integration
4. Data models for PRs and issues

### Phase 2 – Core Desktop App
5. Desktop UI with 3 tabs
6. List rendering
7. Click-to-open behavior

### Phase 3 – Business Logic
8. Indicator computation logic
9. Draft / Approved / Action-required rules

### Phase 4 – Status Bar App
10. Status bar icon
11. Popup UI mirroring desktop tabs

### Phase 5 – State & Account Management
12. Logout functionality
13. Error recovery to login screen

---

## 13. Account Management

### Logout

- User can explicitly log out
- On logout:
  - Stored repository URL is cleared
  - Stored Personal Access Token is cleared
  - App returns to the login/configuration screen

---

## 14. Summary

This product is a **focused, action-oriented GitHub companion** designed to:
- Eliminate tab chaos
- Reduce manual checking
- Clearly surface what needs attention

V1 prioritizes clarity, simplicity, and speed over completeness.
