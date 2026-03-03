package co.kofixtures.kotest.arb.utils

import co.kofixtures.core.Generator
import co.kofixtures.core.fixtureModule
import co.kofixtures.core.register
import co.kofixtures.kotest.arb.getArb
import co.kofixtures.kotest.arb.register
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string
import kotlin.random.Random

val random = Random(seed = 42)

fun <T> gen(block: (Random) -> T) = Generator(block)

data class Person(
    val name: String,
    val age: Int,
)

data class Project(
    val name: String,
    val description: String,
)

val personModule = fixtureModule {
    register<String> { Arb.string(3..3) }
    register<Int> { gen { 10 } }
    register<Person> {
        val nameArb = getArb(Person::name)
        val ageArb = getArb(Person::age)
        arbitrary { Person(nameArb.bind(), ageArb.bind()) }
    }
}
val projectModule = fixtureModule {
    register<String> { Arb.string(4..4) }
    register<Project> {
        val nameArb = getArb(Project::name)
        val descriptionArb = getArb(Project::description)
        arbitrary { Project(nameArb.bind(), descriptionArb.bind()) }
    }
}
