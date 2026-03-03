rootProject.name = "kofixture"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":core")
include(":kofixture-kotest-arb")
include(":kofixture-annotations")
include(":kofixture-ksp")
