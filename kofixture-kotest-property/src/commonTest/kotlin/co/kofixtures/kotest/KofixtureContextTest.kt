package co.kofixtures.kotest

import co.kofixtures.core.fixtureModule
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string

data class Person(
    val name: String,
    val age: Int,
)

data class User(
    val name: String,
    val surname: String,
)

private val personModule =
    fixtureModule {
        kotest {
            register<String>(Arb.string(3..10))
            register<Int>(Arb.int(1..99))
            register<Person> {
                val nameArb = getArb(Person::name)
                val ageArb = getArb<Int>()
                arbitrary { Person(nameArb.bind(), ageArb.bind()) }
            }
        }
    }

class KofixtureContextTest :
    FreeSpec(),
    KofixtureTest {
    override val fixtureModules = listOf(personModule)
    override val extensions: List<Extension>
        get() = super.extensions + KofixtureListener(this)
}
