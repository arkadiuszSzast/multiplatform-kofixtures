package co.kofixtures.kotest.arb

import co.kofixtures.core.KofixtureContext
import co.kofixtures.core.KofixtureTest
import co.kofixtures.core.OverrideScope
import io.kotest.property.Arb
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

fun <T> KofixtureTest.arb(
    type: KType,
    tag: String? = null,
    block: OverrideScope.() -> Unit = {},
): ReadOnlyProperty<Any?, Arb<T>> = ReadOnlyProperty { _, _ ->
    val registry = KofixtureContext.registryFor(this)
    registry.resolve<T>(type, tag, block).asArb()
}

inline fun <reified T> KofixtureTest.arb(
    tag: String? = null,
    noinline block: OverrideScope.() -> Unit = {},
): ReadOnlyProperty<Any?, Arb<T>> = arb(typeOf<T>(), tag, block)
