@file:OptIn(org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi::class)

package co.kofixtures.ksp

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class KoFixtureProcessorTest {

    @Test
    fun `generates FixtureModule val for annotated object`() {
        val source = SourceFile.kotlin(
            "Domain.kt",
            """
            package co.example.domain

            import co.kofixtures.ksp.KoFixture

            data class Project(val name: String, val description: String)

            @KoFixture(packages = ["co.example.domain"])
            object DomainFixtures
            """.trimIndent(),
        )

        val result = compile(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        val generated = result.kspGeneratedKtFiles()
        assertTrue(
            generated.any { it.name == "DomainFixturesGenerated.kt" },
            "Expected DomainFixturesGenerated.kt, got: ${generated.map { it.name }}",
        )
        val content = generated.first { it.name == "DomainFixturesGenerated.kt" }.readText()
        assertTrue(content.contains("val domainFixtures: FixtureModule = fixtureModule {"), content)
        assertTrue(content.contains("register<co.example.domain.Project>"), content)
        assertTrue(content.contains("sample(co.example.domain.Project::name, random)"), content)
        assertTrue(content.contains("sample(co.example.domain.Project::description, random)"), content)
    }

    @Test
    fun `sealed subtypes are registered before the sealed parent`() {
        val source = SourceFile.kotlin(
            "Domain.kt",
            """
            package co.example.domain

            import co.kofixtures.ksp.KoFixture

            sealed class Status {
                object Active : Status()
                object Inactive : Status()
                data class Custom(val value: String) : Status()
            }

            @KoFixture(packages = ["co.example.domain"])
            object DomainFixtures
            """.trimIndent(),
        )

        val result = compile(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        val content = result.kspGeneratedKtFiles()
            .first { it.name == "DomainFixturesGenerated.kt" }
            .readText()

        listOf("Status.Active", "Status.Inactive", "Status.Custom", "Status").forEach { type ->
            assertTrue(content.contains("register<co.example.domain.$type>"), "Missing $type in:\n$content")
        }

        // Subtypes must appear before the sealed parent
        val activeIdx = content.indexOf("register<co.example.domain.Status.Active>")
        val parentIdx = content.indexOf("register<co.example.domain.Status>")
        assertTrue(activeIdx < parentIdx, "Status.Active must be registered before Status")
    }

    @Test
    fun `generates enum registration using entries`() {
        val source = SourceFile.kotlin(
            "Domain.kt",
            """
            package co.example.domain

            import co.kofixtures.ksp.KoFixture

            enum class Priority { LOW, MEDIUM, HIGH }

            @KoFixture(packages = ["co.example.domain"])
            object DomainFixtures
            """.trimIndent(),
        )

        val result = compile(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        val content = result.kspGeneratedKtFiles()
            .first { it.name == "DomainFixturesGenerated.kt" }
            .readText()

        assertTrue(content.contains("register<co.example.domain.Priority>"), content)
        assertTrue(content.contains("Priority.entries"), content)
    }

    @Test
    fun `generates object registration returning the singleton`() {
        val source = SourceFile.kotlin(
            "Domain.kt",
            """
            package co.example.domain

            import co.kofixtures.ksp.KoFixture

            object Singleton

            @KoFixture(packages = ["co.example.domain"])
            object DomainFixtures
            """.trimIndent(),
        )

        val result = compile(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        val content = result.kspGeneratedKtFiles()
            .first { it.name == "DomainFixturesGenerated.kt" }
            .readText()

        assertTrue(content.contains("register<co.example.domain.Singleton>"), content)
        assertTrue(content.contains("Generator { _ -> co.example.domain.Singleton }"), content)
    }

    @Test
    fun `skips abstract non-sealed classes`() {
        val source = SourceFile.kotlin(
            "Domain.kt",
            """
            package co.example.domain

            import co.kofixtures.ksp.KoFixture

            abstract class BaseRepo
            data class Project(val name: String) : BaseRepo()

            @KoFixture(packages = ["co.example.domain"])
            object DomainFixtures
            """.trimIndent(),
        )

        val result = compile(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        val content = result.kspGeneratedKtFiles()
            .first { it.name == "DomainFixturesGenerated.kt" }
            .readText()

        assertTrue(content.contains("register<co.example.domain.Project>"), content)
        assertTrue(!content.contains("register<co.example.domain.BaseRepo>"), content)
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private fun compile(vararg sources: SourceFile): JvmCompilationResult {
        val c = KotlinCompilation()
        c.sources = sources.toList()
        c.inheritClassPath = true
        c.jvmTarget = "21"
        c.configureKsp {} // must be called first to initialise KSP tool
        c.symbolProcessorProviders = mutableListOf<SymbolProcessorProvider>(KoFixtureProvider())
        c.kspWithCompilation = true
        return c.compile()
    }

    private fun JvmCompilationResult.kspGeneratedKtFiles(): List<File> = sourcesGeneratedBySymbolProcessor.filter { it.extension == "kt" }.toList()
}
