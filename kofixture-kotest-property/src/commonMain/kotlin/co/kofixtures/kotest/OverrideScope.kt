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

//@JvmName("override_type_arb")
//@OptIn(ExperimentalTypeInference::class)
//@OverloadResolutionByLambdaReturnType
//inline fun <reified T> OverrideScope.override(crossinline block: TypeOverrideScope<T>.() -> Arb<T>) {
//    val factory = { scope: TypeOverrideScope<T> ->
//        val arb = block(scope)
//        ArbGenerator(arb)
//    }
//    addOverride(
//        FixtureOverride.TypeBased(
//            typeOf<T>(),
//            factory,
//        ),
//    )
//}

//@JvmName("override_type_named_arb")
//@OptIn(ExperimentalTypeInference::class)
//@OverloadResolutionByLambdaReturnType
//inline fun <reified Owner : Any, reified Prop> OverrideScope.override(
//    property: KProperty1<Owner, Prop>,
//    crossinline block: PropOverrideScope<Prop>.() -> Prop,
//) {
//    val factory = { scope: PropOverrideScope<T> ->
//        val arb = block(scope)
//        ArbGenerator(arb)
//    }
//    addOverride(
//        FixtureOverride.Named(
//            key = NamedOverrideKey(typeOf<Owner>(), property.name),
//            gen = Generator { _ -> PropOverrideScope<Prop>(registry).block() },
//        ),
//    )
//}
