package co.example

import co.example.domain.Article
import co.example.domain.Role
import co.example.domain.Status
import co.example.domain.User
import co.example.domain.domainFixtures
import co.kofixtures.core.Generator
import co.kofixtures.core.buildRegistry
import co.kofixtures.core.override
import co.kofixtures.core.register
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

/**
 * Shows how to use the KSP-generated domainFixtures module.
 *
 * Setup:
 *   1. Register leaf types (primitives your domain doesn't own: String, Int, ...).
 *   2. Call includes(domainFixtures) — the generated val that registers every
 *      class discovered in co.example.domain automatically.
 *   3. Call registry.sample<YourType>() anywhere in tests.
 */
class DomainFixtureTest {

    private val registry = buildRegistry {
        register<Int> { Generator { random -> random.nextInt(100_000) } }
        register<String> { Generator { random -> "str-${random.nextInt(100_000)}" } }
        includes(domainFixtures)
    }

    @Test
    fun `samples User with correct types`() {
        val user = registry.sample<User>()

        user.name shouldStartWith "str-"
        user.id shouldNotBe null
        user.role shouldNotBe null
    }

    @Test
    fun `samples Article including nested User`() {
        val article = registry.sample<Article>()

        article.title shouldStartWith "str-"
        article.author.name shouldStartWith "str-"
        article.status shouldNotBe null
    }

    @Test
    fun `samples all Role values across many draws`() {
        val seen = (1..60).map { registry.sample<Role>() }.toSet()
        seen shouldContainAll Role.entries
    }

    @Test
    fun `samples all Status variants across many draws`() {
        val seen = (1..60).map { registry.sample<Status>().javaClass.simpleName }.toSet()
        seen shouldContainAll listOf("Draft", "Published", "Rejected")
    }

    @Test
    fun `Status_Rejected always has a non-null reason`() {
        val rejected = generateSequence { registry.sample<Status>() }
            .filterIsInstance<Status.Rejected>()
            .first()

        rejected.reason shouldNotBe null
        rejected.shouldBeInstanceOf<Status.Rejected>()
    }
}
