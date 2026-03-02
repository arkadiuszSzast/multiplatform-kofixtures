package co.kofixtures.kotest

import co.kofixtures.core.FixtureOverride
import co.kofixtures.core.NamedOverrideKey
import co.kofixtures.core.OverrideScope
import io.kotest.property.Arb
import kotlin.reflect.KProperty1
import kotlin.reflect.typeOf

// ---------------------------------------------------------------------------
// Arb-based override extensions on OverrideScope (kotest-property sugar)
// ---------------------------------------------------------------------------

/** Overrides all fields of type [T] with values from [arb]. */
inline fun <reified T> OverrideScope.override(arb: Arb<T>) {
    addOverride(FixtureOverride.TypeBased(typeOf<T>(), arb.asGenerator()))
}

/** Overrides all fields of type [T] with values from [arb]. */
inline fun <reified T> OverrideScope.overrideA(arbProvider: () -> Arb<T>) {
    addOverride(FixtureOverride.TypeBased(typeOf<T>(), arbProvider().asGenerator()))
}

/** Overrides a specific [property] on [Owner] with values from [arb]. */
inline fun <reified Owner : Any, reified Prop> OverrideScope.override(
    property: KProperty1<Owner, Prop>,
    arb: Arb<Prop>,
) {
    addOverride(
        FixtureOverride.Named(
            key = NamedOverrideKey(typeOf<Owner>(), property.name),
            gen = arb.asGenerator(),
        ),
    )
}
