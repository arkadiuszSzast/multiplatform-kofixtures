package co.kofixtures.core

import kotlin.random.Random
import kotlin.reflect.KProperty1
import kotlin.reflect.typeOf

/**
 * Scope available inside a factory lambda during resolve.
 * Provides access to other generators via [get], respecting active overrides.
 *
 * Core usage:
 *   buildRegistry {
 *       register<Person> { scope ->
 *           val nameGen = scope.get<String>()
 *           Generator { rng -> Person(nameGen.next(rng)) }
 *       }
 *   }
 *
 * Extended in kofixture-kotest with getArb() sugar.
 */
class FactoryScope(val registry: FixtureRegistry, val activeOverrides: ActiveOverrides) {

    inline fun <reified Owner : Any, reified Prop> get(
        property: KProperty1<Owner, Prop>,
        tag: String? = null,
    ): Generator<Prop> {
        val namedKey = NamedOverrideKey(typeOf<Owner>(), property.name)
        @Suppress("UNCHECKED_CAST")
        return activeOverrides.resolveNamed(namedKey) as? Generator<Prop>
            ?: registry.resolve(typeOf<Prop>(), tag, activeOverrides)
    }

    inline fun <reified Owner : Any, reified Prop> sample(
        property: KProperty1<Owner, Prop>,
        random: Random = Random,
        tag: String? = null,
    ): Prop = get(property, tag).next(random)
}