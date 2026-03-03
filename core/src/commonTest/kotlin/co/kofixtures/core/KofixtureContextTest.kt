package co.kofixtures.core

import co.kofixtures.core.utils.gen
import co.kofixtures.core.utils.random
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

private val stringModule = fixtureModule {
    register<String> { gen { "text" } }
}

class KofixtureContextTest : FreeSpec(), KofixtureTest {
    override val fixtureModules = listOf(stringModule)

    override suspend fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        buildRegistry()
    }

    init {
        "should generate string" {
            registry().sample<String>() shouldBe "text"
        }

        "should generate generate person using delegate" {
            val string: String by sample()
            string shouldBe "text"
        }

        "should get person generator using delegate" {
            val personGenerator: Generator<String> by generator()
            val string = personGenerator.next(random)
            string shouldBe "text"
        }
    }
}
