package co.kofixtures.kotest

import co.kofixtures.core.FactoryScope
import co.kofixtures.core.FixtureRegistryBuilder
import io.kotest.property.Arb
import kotlin.reflect.typeOf

/** Arb<T> sugar for FactoryScope — available in kotest { register { } } blocks. */
inline fun <reified T> FactoryScope.getArb(tag: String? = null): Arb<T> = get<T>(tag).asArb()

inline fun <reified Owner : Any, reified Prop> FactoryScope.getArb(property: kotlin.reflect.KProperty1<Owner, Prop>): Arb<Prop> = get(property).asArb()

/**
 * Scope for kotest { } block — registers Arb-based generators.
 *
 *   buildRegistry {
 *       kotest {
 *           register<String> { Arb.string(3..15) }
 *           register<Person> {
 *               val nameArb = getArb(Person::name)
 *               arbitrary { Person(nameArb.bind()) }
 *           }
 *       }
 *   }
 */
class KotestScope(
    @PublishedApi internal val builder: FixtureRegistryBuilder,
) {
    /** Registers a plain Arb — no dependencies on other generators. */
    inline fun <reified T> register(
        arb: Arb<T>,
        tag: String? = null,
    ) = builder.register(typeOf<T>(), tag) { arb.asGenerator() }

    /** Registers an Arb factory with access to other generators via [FactoryScope]. */
    inline fun <reified T> register(
        tag: String? = null,
        crossinline block: FactoryScope.() -> Arb<T>,
    ) = builder.register(typeOf<T>(), tag) { scope ->
        scope.block().asGenerator()
    }
}

fun FixtureRegistryBuilder.kotest(block: KotestScope.() -> Unit) = KotestScope(this).block()
