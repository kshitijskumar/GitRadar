package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import org.example.project.util.PlatformContext

fun MainViewController() = ComposeUIViewController { App(platformContext = PlatformContext()) }