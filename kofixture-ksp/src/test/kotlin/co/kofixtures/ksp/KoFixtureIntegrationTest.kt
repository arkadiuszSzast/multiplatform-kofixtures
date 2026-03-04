@file:OptIn(org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi::class)

package co.kofixtures.ksp

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Method

/**
 * Integration tests from the *client perspective*: each test compiles real domain
 * classes with @KoFixture, then invokes sampling helpers via reflection to confirm
 * that the generated registry correctly builds and samples typed values.
 *
 * Pattern: the compiled source includes a helper function that wires up the full
 * registry (basic leaf-type generators + the KSP-generated DomainFixtures block)
 * and returns a sampled value. We invoke these helpers via reflection.
 */
class KoFixtureIntegrationTest {

    @Test
    fun `samples data class with correctly typed fields`() {
        val result = compile(
            SourceFile.kotlin(
                "Domain.kt",
                """
                package co.example.domain

                import co.kofixtures.ksp.KoFixture
                import co.kofixtures.core.buildRegistry
                import co.kofixtures.core.register
                import co.kofixtures.core.Generator

                data class Project(val name: String, val description: String)

                @KoFixture(packages = ["co.example.domain"])
                object DomainFixtures

                fun sampleProject(): Any = buildRegistry {
                    register<String> { Generator { random -> "s${'$'}{random.nextInt(10_000)}" } }
                    includes(domainFixtures)
                }.sample<Project>()
                """.trimIndent(),
            ),
        )

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val project = result.invoke("sampleProject")
        val projectClass = result.classLoader.loadClass("co.example.domain.Project")
        assertTrue(projectClass.isInstance(project), "Expected Project, got: $project")

        val name = projectClass.getDeclaredMethod("getName").invoke(project)
        val description = projectClass.getDeclaredMethod("getDescription").invoke(project)
        assertTrue(name is String, "name should be String, was: $name")
        assertTrue(description is String, "description should be String, was: $description")
        assertTrue((name as String).startsWith("s"), "name should match generator pattern, was: $name")
    }

    @Test
    fun `samples sealed class — cycles through all subtypes`() {
        val result = compile(
            SourceFile.kotlin(
                "Domain.kt",
                """
                package co.example.domain

                import co.kofixtures.ksp.KoFixture
                import co.kofixtures.core.buildRegistry
                import co.kofixtures.core.register
                import co.kofixtures.core.Generator

                sealed class Status {
                    object Active   : Status()
                    object Inactive : Status()
                    data class Custom(val value: String) : Status()
                }

                @KoFixture(packages = ["co.example.domain"])
                object DomainFixtures

                fun sampleStatus(): Any = buildRegistry {
                    register<String> { Generator { random -> "v${'$'}{random.nextInt(1000)}" } }
                    includes(domainFixtures)
                }.sample<Status>()
                """.trimIndent(),
            ),
        )

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val statusClass = result.classLoader.loadClass("co.example.domain.Status")
        val sampleFn = result.helperMethod("sampleStatus")
        val seen = mutableSetOf<String>()

        repeat(60) {
            val status = sampleFn.invoke(null)
            assertTrue(statusClass.isInstance(status), "Expected Status, got: $status")
            seen.add(status!!.javaClass.simpleName)
        }

        // All three subtypes must appear in 60 draws (each has 1/3 probability)
        setOf("Active", "Inactive", "Custom").forEach { name ->
            assertTrue(name in seen, "$name never produced in 60 samples. Seen: $seen")
        }
    }

    @Test
    fun `samples enum — every constant appears across many draws`() {
        val result = compile(
            SourceFile.kotlin(
                "Domain.kt",
                """
                package co.example.domain

                import co.kofixtures.ksp.KoFixture
                import co.kofixtures.core.buildRegistry

                enum class Priority { LOW, MEDIUM, HIGH }

                @KoFixture(packages = ["co.example.domain"])
                object DomainFixtures

                fun samplePriority(): Any = buildRegistry { includes(domainFixtures) }.sample<Priority>()
                """.trimIndent(),
            ),
        )

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val priorityClass = result.classLoader.loadClass("co.example.domain.Priority")
        assertTrue(priorityClass.isEnum)

        val sampleFn = result.helperMethod("samplePriority")
        val seen = mutableSetOf<String>()
        repeat(60) { seen.add((sampleFn.invoke(null) as Enum<*>).name) }

        setOf("LOW", "MEDIUM", "HIGH").forEach { name ->
            assertTrue(name in seen, "$name never produced in 60 samples. Seen: $seen")
        }
    }

    @Test
    fun `samples standalone object — always returns the same singleton`() {
        val result = compile(
            SourceFile.kotlin(
                "Domain.kt",
                """
                package co.example.domain

                import co.kofixtures.ksp.KoFixture
                import co.kofixtures.core.buildRegistry

                object Singleton

                @KoFixture(packages = ["co.example.domain"])
                object DomainFixtures

                fun sampleSingleton(): Any = buildRegistry { includes(domainFixtures) }.sample<Singleton>()
                """.trimIndent(),
            ),
        )

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val singletonClass = result.classLoader.loadClass("co.example.domain.Singleton")
        val expected = singletonClass.getDeclaredField("INSTANCE").get(null)
        val sampleFn = result.helperMethod("sampleSingleton")

        repeat(5) {
            assertEquals(expected, sampleFn.invoke(null), "Singleton must always be the INSTANCE")
        }
    }

    @Test
    fun `registry with multiple domain types — each resolves correctly`() {
        val result = compile(
            SourceFile.kotlin(
                "Domain.kt",
                """
                package co.example.domain

                import co.kofixtures.ksp.KoFixture
                import co.kofixtures.core.buildRegistry
                import co.kofixtures.core.register
                import co.kofixtures.core.FixtureRegistry
                import co.kofixtures.core.Generator

                data class Project(val name: String)
                data class User(val id: Int, val email: String)
                enum class Role { ADMIN, VIEWER }

                @KoFixture(packages = ["co.example.domain"])
                object DomainFixtures

                fun registry(): FixtureRegistry = buildRegistry {
                    register<String> { Generator { random -> "s${'$'}{random.nextInt(9_999)}" } }
                    register<Int>    { Generator { random -> random.nextInt(9_999) } }
                    includes(domainFixtures)
                }
                fun sampleProject(): Any = registry().sample<Project>()
                fun sampleUser():    Any = registry().sample<User>()
                fun sampleRole():    Any = registry().sample<Role>()
                """.trimIndent(),
            ),
        )

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val projectClass = result.classLoader.loadClass("co.example.domain.Project")
        val userClass = result.classLoader.loadClass("co.example.domain.User")
        val roleClass = result.classLoader.loadClass("co.example.domain.Role")

        assertTrue(projectClass.isInstance(result.invoke("sampleProject")))
        assertTrue(userClass.isInstance(result.invoke("sampleUser")))
        assertTrue(roleClass.isInstance(result.invoke("sampleRole")))
    }

    @Test
    fun `nested sealed hierarchy — intermediate and top levels both resolve`() {
        val result = compile(
            SourceFile.kotlin(
                "Domain.kt",
                """
                package co.example.domain

                import co.kofixtures.ksp.KoFixture
                import co.kofixtures.core.buildRegistry
                import co.kofixtures.core.register
                import co.kofixtures.core.Generator

                sealed class Result {
                    sealed class Err : Result() {
                        data class Network(val code: Int) : Err()
                        data class Parse(val msg: String)  : Err()
                    }
                    data class Ok(val data: String) : Result()
                }

                @KoFixture(packages = ["co.example.domain"])
                object DomainFixtures

                private fun reg() = buildRegistry {
                    register<String> { Generator { random -> "s${'$'}{random.nextInt(9_999)}" } }
                    register<Int>    { Generator { random -> random.nextInt(9_999) } }
                    includes(domainFixtures)
                }
                fun sampleResult(): Any = reg().sample<Result>()
                fun sampleErr():    Any = reg().sample<Result.Err>()
                """.trimIndent(),
            ),
        )

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val resultClass = result.classLoader.loadClass("co.example.domain.Result")
        val errClass = result.classLoader.loadClass("co.example.domain.Result\$Err")
        val sampleResult = result.helperMethod("sampleResult")
        val sampleErr = result.helperMethod("sampleErr")

        repeat(20) {
            assertTrue(resultClass.isInstance(sampleResult.invoke(null)))
            assertTrue(errClass.isInstance(sampleErr.invoke(null)))
        }
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private fun compile(vararg sources: SourceFile): JvmCompilationResult {
        val c = KotlinCompilation()
        c.sources = sources.toList()
        c.inheritClassPath = true
        c.jvmTarget = "21"
        c.configureKsp {}
        c.symbolProcessorProviders = mutableListOf<SymbolProcessorProvider>(KoFixtureProvider())
        c.kspWithCompilation = true
        return c.compile()
    }

    /** Call a top-level helper fun from the compiled [Domain.kt] via reflection. */
    private fun JvmCompilationResult.invoke(name: String): Any? = helperMethod(name).invoke(null)

    private fun JvmCompilationResult.helperMethod(name: String): Method {
        val cls = classLoader.loadClass("co.example.domain.DomainKt")
        return cls.getDeclaredMethod(name).also { assertNotNull(it) }
    }
}
