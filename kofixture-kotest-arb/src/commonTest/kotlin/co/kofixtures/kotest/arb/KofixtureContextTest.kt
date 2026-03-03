package co.kofixtures.kotest.arb

import co.kofixtures.core.KofixtureTest
import co.kofixtures.kotest.arb.utils.Person
import co.kofixtures.kotest.arb.utils.personModule
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class KofixtureContextTest : FreeSpec(), KofixtureTest {
    override val fixtureModules = listOf(personModule)

    override suspend fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        buildRegistry()
    }

    init {
        "should get person arb using delegate" {
            val personArb: Arb<Person> by arb()
            val person = personArb.next()
            person.name.shouldHaveLength(3)
            person.age shouldBe 10
        }
    }
}
