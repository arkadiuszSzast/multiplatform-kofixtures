package co.kofixtures.core

import co.kofixtures.core.utils.gen
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlin.random.Random

data class Person(val name: Name, val age: Int, val status: String)

@JvmInline
value class Name(val value: String)

class RegistryJvmTest : FreeSpec({

    val registry = buildRegistry {
        register<String> { gen { "text" } }
        register<Int> { gen { 42 } }
        registerOf(::Name)
        registerOf(::Person)
    }

    "can resolve auto created fixture" {
        registry.generator<Person>().next(Random) shouldBe Person(Name("text"), 42, "text")
    }

    "can override generator by type" {
        registry.sample<Person> {
            override<String> { "override" }
        } shouldBe Person(Name("override"), 42, "override")
    }

    "can override by property" {
        registry.sample<Person> {
            override(Person::status) { "active" }
        } shouldBe Person(Name("text"), 42, "active")
    }
})
