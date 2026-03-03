package co.kofixtures.kotest.arb

import co.kofixtures.core.FactoryScope
import co.kofixtures.core.FixtureRegistryBuilder
import co.kofixtures.core.register
import io.kotest.property.Arb
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KProperty1

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
inline fun <reified T> FixtureRegistryBuilder.register(
    tag: String? = null,
    crossinline block: FactoryScope.() -> Arb<T>,
) {
    val factory = { factoryScope: FactoryScope ->
        val arb = block(factoryScope)
        ArbGenerator(arb)
    }
    register<T>(tag, factory)
}

inline fun <reified Owner : Any, reified Prop> FactoryScope.getArb(
    property: KProperty1<Owner, Prop>,
    tag: String? = null,
) = get(property, tag).asArb()
