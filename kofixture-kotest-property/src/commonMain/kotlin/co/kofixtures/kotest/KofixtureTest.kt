package co.kofixtures.kotest

import co.kofixtures.core.FixtureModule
import co.kofixtures.core.OverrideScope
import io.kotest.property.Arb
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface KofixtureTest {
    val fixtureModules: List<FixtureModule> get() = emptyList()

    fun <T> sample(
        type: KType,
        tag: String? = null,
        block: OverrideScope.() -> Unit = {},
    ): ReadOnlyProperty<Any?, T> = ReadOnlyProperty { _, _ ->
        val registry = KofixtureContext.registryFor(this)
        registry
            .resolveWithScope<T>(type, tag, OverrideScope(registry).apply(block))
            .next(kotlin.random.Random.Default)
    }

    fun <T> arb(
        type: KType,
        tag: String? = null,
        block: OverrideScope.() -> Unit = {},
    ): ReadOnlyProperty<Any?, Arb<T>> = ReadOnlyProperty { _, _ ->
        val registry = KofixtureContext.registryFor(this)
        registry
            .resolveWithScope<T>(type, tag, OverrideScope(registry).apply(block))
            .asArb()
    }
}

inline fun <reified T> KofixtureTest.sample(
    tag: String? = null,
    noinline block: OverrideScope.() -> Unit = {},
): ReadOnlyProperty<Any?, T> = sample(typeOf<T>(), tag, block)

inline fun <reified T> KofixtureTest.arb(
    tag: String? = null,
    noinline block: OverrideScope.() -> Unit = {},
): ReadOnlyProperty<Any?, Arb<T>> = arb(typeOf<T>(), tag, block)
