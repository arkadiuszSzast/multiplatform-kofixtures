package co.kofixtures.core

import kotlin.properties.ReadOnlyProperty
import kotlin.random.Random
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface KofixtureTest {
    val fixtureModules: List<FixtureModule> get() = emptyList()

    fun registry(): FixtureRegistry = KofixtureContext.registryFor(this)
    fun buildRegistry() = KofixtureContext.buildFor(this)

    fun <T> sample(
        type: KType,
        tag: String? = null,
        random: Random = Random,
        block: OverrideScope.() -> Unit = {},
    ): ReadOnlyProperty<Any?, T> = ReadOnlyProperty { _, _ ->
        val registry = KofixtureContext.registryFor(this)
        registry.resolve<T>(type, tag, block).next(random)
    }

    fun <T> generator(
        type: KType,
        tag: String? = null,
        random: Random = Random,
        block: OverrideScope.() -> Unit = {},
    ): ReadOnlyProperty<Any?, Generator<T>> = ReadOnlyProperty { _, _ ->
        val registry = KofixtureContext.registryFor(this)
        registry.resolve<T>(type, tag, block)
    }
}

inline fun <reified T> KofixtureTest.sample(
    tag: String? = null,
    random: Random = Random,
    noinline block: OverrideScope.() -> Unit = {},
): ReadOnlyProperty<Any?, T> = sample(typeOf<T>(), tag, random, block)

inline fun <reified T> KofixtureTest.generator(
    tag: String? = null,
    random: Random = Random,
    noinline block: OverrideScope.() -> Unit = {},
): ReadOnlyProperty<Any?, Generator<T>> = generator(typeOf<T>(), tag, random, block)
