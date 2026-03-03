plugins {
    id("kofixture-kmp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            api(libs.kotest.property)
        }
        commonTest.dependencies {
            implementation(libs.kotest.framework.engine)
        }
        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5)
        }
    }
}
