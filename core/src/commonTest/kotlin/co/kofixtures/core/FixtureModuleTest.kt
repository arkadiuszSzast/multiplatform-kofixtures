package co.kofixtures.core

import co.kofixtures.core.utils.Person
import co.kofixtures.core.utils.gen
import co.kofixtures.core.utils.random
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlin.random.Random

class FixtureModuleTest : FreeSpec({

    "can combine multiple modules" {
        val intModule = fixtureModule {
            register<Int> { gen { 1 } }
        }
        val stringModule = fixtureModule {
            register<String> { gen { "text" } }
        }
        val registry = buildRegistry {
            includes(intModule, stringModule)
        }
        registry.generator<Int>().next(random) shouldBe 1
        registry.generator<String>().next(random) shouldBe "text"
    }

    "latest module overrides previous" {
        val oldModule = fixtureModule {
            register<Int> { gen { 1 } }
        }
        val newModule = fixtureModule {
            register<Int> { gen { 2 } }
        }
        val registry = buildRegistry {
            includes(oldModule, newModule)
        }
        registry.generator<Int>().next(random) shouldBe 2
    }

    "can use generators defined in other module during registration" {
        val intModule = fixtureModule {
            register<Int> { gen { 1 } }
        }
        val stringModule = fixtureModule {
            register<String> {
                val intGenerator = get<Int>()
                gen { intGenerator.next(it).toString() }
            }
        }
        val registry = buildRegistry {
            includes(intModule, stringModule)
        }
        registry.generator<String>().next(random) shouldBe "1"
    }

    "can use tagged generators defined in other module" {
        val intModule = fixtureModule {
            register<Int>(tag = "one") { gen { 1 } }
        }
        val stringModule = fixtureModule {
            register<String> {
                val intGenerator = get<Int>(tag = "one")
                gen { intGenerator.next(it).toString() }
            }
        }
        val registry = buildRegistry {
            includes(intModule, stringModule)
        }
        registry.generator<String>().next(random) shouldBe "1"
    }

    "can combine generators from multiple modules to create new generator" {
        val intModule = fixtureModule {
            register<Int> { gen { 20 } }
        }
        val stringModule = fixtureModule {
            register<String> { gen { "Joe" } }
            register<Person> { gen { Person(get<String>().next(it), get<Int>().next(it)) } }
        }
        val registry = buildRegistry {
            includes(intModule, stringModule)
        }
        registry.generator<Person>().next(random) shouldBe Person("Joe", 20)
    }
})