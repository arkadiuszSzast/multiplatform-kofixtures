package co.kofixtures.kotest

import co.kofixtures.core.buildRegistry
import co.kofixtures.core.register
import co.kofixtures.kotest.register
import co.kofixtures.kotest.utils.Person
import co.kofixtures.kotest.utils.gen
import co.kofixtures.kotest.utils.random
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

class RegistryTest : FreeSpec({

    "should register arb generator" {
        val registry = buildRegistry {
            register<String> { Arb.string(3..3) }
        }
        registry.generator<String>().next(random).shouldHaveLength(3)
    }

    "can mix generator" {
        val registry = buildRegistry {
            register<String> { Arb.string(3..3) }
            register<Int> { gen { 10 } }
            register<Person> {
                val nameArb = getArb(Person::name)
                val ageArb = getArb(Person::age)
                arbitrary {
                    Person(nameArb.bind(), ageArb.bind())
                }
            }
        }
        registry.generator<Person>().next(random).should {
            it.name.shouldHaveLength(3)
            it.age shouldBe 10
        }
    }
})
