plugins {
    // kotlin.jvm is already on the classpath via buildSrc — use id without version
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.ksp)
}

dependencies {
    compileOnly(libs.ksp.api)
    implementation(project(":core"))
    implementation(project(":kofixture-annotations"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
