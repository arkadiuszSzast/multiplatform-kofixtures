package co.kofixtures.kotest

import co.kofixtures.core.FactoryScope
import co.kofixtures.core.FixtureOverride
import co.kofixtures.core.Generator
import co.kofixtures.core.NamedOverrideKey
import co.kofixtures.core.OverrideScope
import co.kofixtures.core.PropOverrideScope
import co.kofixtures.core.TypeOverrideScope
import io.kotest.property.Arb
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty1
import kotlin.reflect.typeOf

@JvmName("override_type_arb")
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
inline fun <reified T> OverrideScope.override(block: TypeOverrideScope<T>.() -> Arb<T>) {
    addOverride(
        FixtureOverride.TypeBased(
            typeOf<T>(),
            ArbGenerator(TypeOverrideScope<T>(registry).block()),
        ),
    )
}

@JvmName("override_property_arb")
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
inline fun <reified Owner : Any, reified Prop> OverrideScope.override(
    property: KProperty1<Owner, Prop>,
    crossinline block: PropOverrideScope<Prop>.() -> Arb<Prop>,
) {
    addOverride(
        FixtureOverride.Named(
            key = NamedOverrideKey(typeOf<Owner>(), property.name),
            gen = ArbGenerator(PropOverrideScope<Prop>(registry).block()),
        ),
    )
}
