## TL;DR
This is a very simple macOS application to solve a personal problem of managing my pending PRs and PRs assigned to me for review.

## Problem statement
I keep multiple GitHub tabs open to:
- Review PRs assigned to me
- Monitor my own PRs for review feedback or approval status

This leads to:
- Context switching
- Tab clutter
- Manual and repetitive checks
- Missed updates (new comments, commits after review)

## Solution - GitRadar
A lightweight **macOS application** that aggregates GitHub activity into a single, action-focused dashboard.

The app presents only what matters:
- What needs action
- What is you caught up with
- What is still a draft

No noise, no replacement of GitHub—just a productivity layer on top.


<img width="1006" height="689" alt="Screenshot 2026-02-01 at 8 05 26 PM" src="https://github.com/user-attachments/assets/18777b94-f9f8-4ec6-8344-6cd7a99b8fed" />

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```
