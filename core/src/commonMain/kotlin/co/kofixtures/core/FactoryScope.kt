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

    /** Resolves [T] from registry, respecting active type-based overrides. */
    inline fun <reified T> get(tag: String? = null): Generator<T> =
        registry.resolve(typeOf<T>(), tag, activeOverrides)

    inline fun <reified T> sample(random: Random = Random, tag: String? = null): T =
        get<T>(tag).next(random)
}