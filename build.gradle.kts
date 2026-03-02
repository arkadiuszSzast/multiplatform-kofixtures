plugins {
    alias(libs.plugins.spotless)
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(
            mapOf(
                "ktlint_function_signature_body_expression_wrapping" to "false",
                "ktlint_standard_class-signature" to "disabled",
            ),
        )
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**/*.gradle.kts")
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(
            mapOf(
                "ktlint_function_signature_body_expression_wrapping" to "false",
                "ktlint_standard_class-signature" to "disabled",
            ),
        )
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
}
