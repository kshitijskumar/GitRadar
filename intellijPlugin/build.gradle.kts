plugins {
    kotlin("jvm")
    alias(libs.plugins.intellijPlatform)
}

repositories {
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

    intellijPlatform {
        local("/Applications/Android Studio.app")
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
