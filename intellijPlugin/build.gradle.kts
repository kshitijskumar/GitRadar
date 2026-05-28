plugins {
    kotlin("jvm")
    alias(libs.plugins.intellijPlatform)
}

repositories {
    google {
        mavenContent {
            includeGroupAndSubgroups("androidx")
            includeGroupAndSubgroups("com.android")
            includeGroupAndSubgroups("com.google")
        }
    }
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.androidx.lifecycle.viewmodel)

    intellijPlatform {
        intellijIdeaCommunity("2024.3")
        bundledPlugin("Git4Idea")
        pluginVerifier()
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        id = "com.github.kshitijskumar.gitradar"
        name = "GitRadar"
        version = "0.1.0-dev"
        description = "GitHub PR review dashboard inside IntelliJ-based IDEs."

        ideaVersion {
            sinceBuild = "243"
        }
    }
}
