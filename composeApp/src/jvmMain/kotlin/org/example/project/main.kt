package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.project.util.PlatformContext

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "GitRadar",
    ) {
        App(platformContext = PlatformContext())
    }
}