package co.kofixtures.kotest

import co.kofixtures.core.Generator
import co.kofixtures.kotest.utils.Person
import co.kofixtures.kotest.utils.personModule
import co.kofixtures.kotest.utils.random
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class KofixtureContextTest : FreeSpec(), KofixtureTest {
    override val fixtureModules = listOf(personModule)
    override val extensions: List<Extension>
        get() = super.extensions + KofixtureListener(this)

    init {
        "should generate generate person" {
            registry().sample<Person>().should {
                it.name.shouldHaveLength(3)
                it.age shouldBe 10
            }
        }

        "should generate generate person using delegate" {
            val person: Person by sample()
            person.name.shouldHaveLength(3)
            person.age shouldBe 10
        }

        "should get person arb using delegate" {
            val personArb: Arb<Person> by arb()
            val person = personArb.next()
            person.name.shouldHaveLength(3)
            person.age shouldBe 10
        }

        "should get person generator using delegate" {
            val personGenerator: Generator<Person> by generator()
            val person = personGenerator.next(random)
            person.name.shouldHaveLength(3)
            person.age shouldBe 10
        }
    }
}
