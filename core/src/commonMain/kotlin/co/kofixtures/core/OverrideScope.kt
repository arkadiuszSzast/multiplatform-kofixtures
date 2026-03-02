package co.kofixtures.core

import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty1
import kotlin.reflect.typeOf


class PropOverrideScope<Prop>(val registry: FixtureRegistry) {
    fun gen(block: () -> Prop): Generator<Prop> = Generator { _ -> block() }
}

class TypeOverrideScope<T>(val registry: FixtureRegistry) {
    fun gen(block: () -> T): Generator<T> = Generator { _ -> block() }
}

class OverrideScope(val registry: FixtureRegistry) {
    @PublishedApi
    internal val fixturesOverrides = mutableListOf<FixtureOverride>()

    private var collectionConfig: CollectionConfig? = null

    fun collections(block: CollectionConfigBuilder.() -> Unit) {
        collectionConfig = CollectionConfigBuilder().apply(block).build()
    }

    @JvmName("override_type")
    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    inline fun <reified T> override(
        crossinline block: TypeOverrideScope<T>.() -> T,
    ) {
        addOverride(FixtureOverride.TypeBased(
            typeOf<T>(),
            Generator { _ -> TypeOverrideScope<T>(registry).block() },
        ))
    }

    @JvmName("override_type_gen")
    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    inline fun <reified T> override(
        block: TypeOverrideScope<T>.() -> Generator<T>,
    ) {
        addOverride(FixtureOverride.TypeBased(
            typeOf<T>(),
            TypeOverrideScope<T>(registry).block(),
        ))
    }

    @JvmName("override_type_named")
    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    inline fun <reified Owner : Any, reified Prop> override(
        property: KProperty1<Owner, Prop>,
        crossinline block: PropOverrideScope<Prop>.() -> Prop,
    ) {
        addOverride(
            FixtureOverride.Named(
                key = NamedOverrideKey(typeOf<Owner>(), property.name),
                gen = Generator { _ -> PropOverrideScope<Prop>(registry).block() },
            )
        )
    }

    @JvmName("override_type_named_gen")
    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    inline fun <reified Owner : Any, reified Prop> override(
        property: KProperty1<Owner, Prop>,
        block: PropOverrideScope<Prop>.() -> Generator<Prop>,
    ) {
        addOverride(
            FixtureOverride.Named(
                key = NamedOverrideKey(typeOf<Owner>(), property.name),
                gen = PropOverrideScope<Prop>(registry).block(),
            )
        )
    }

    fun addOverride(override: FixtureOverride) {
        fixturesOverrides += override
    }

    fun getOverrides() = fixturesOverrides.toList()
    fun getOverriddenCollectionConfig() = collectionConfig
}