plugins {
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":core"))
    testCompileOnly(project(":kofixture-ksp"))
    kspTest(project(":kofixture-ksp"))

    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
