package co.kofixtures.core

import kotlin.jvm.JvmName
import kotlin.reflect.KProperty1
import kotlin.reflect.typeOf

class OverrideScope(val registry: FixtureRegistry) {
    @PublishedApi internal val fixturesOverrides = mutableListOf<FixtureOverride>()

    private var collectionConfig: CollectionConfig? = null

    fun collections(block: CollectionConfigBuilder.() -> Unit) {
        collectionConfig = CollectionConfigBuilder().apply(block).build()
    }

    // --- type-based ---

    @JvmName("override_type")
    inline fun <reified T> override(noinline valueProvider: OverrideScope.() -> T) {
        val value = valueProvider(this)
        addOverride(FixtureOverride.TypeBased(typeOf<T>(), Generator { _ -> value }))
    }

    @JvmName("override_type_gen")
    inline fun <reified T> override(noinline generatorProvider: OverrideScope.() -> Generator<T>) {
        val generator = generatorProvider(this)
        addOverride(FixtureOverride.TypeBased(typeOf<T>(), generator))
    }

    @JvmName("override_type_named")
    inline fun <reified Owner : Any, reified Prop> override(
        property: KProperty1<Owner, Prop>,
        noinline valueProvider: OverrideScope.() -> Prop,
    ) {
        val value = valueProvider(this)
        addOverride(FixtureOverride.Named(
            key = NamedOverrideKey(typeOf<Owner>(), property.name),
            gen = Generator { _ -> value },
        ))
    }

    @JvmName("override_type_named_gen")
    inline fun <reified Owner : Any, reified Prop> override(
        property: KProperty1<Owner, Prop>,
        noinline generatorProvider: OverrideScope.() -> Generator<Prop>,
    ) {
        val generator = generatorProvider(this)
        addOverride(FixtureOverride.Named(
            key = NamedOverrideKey(typeOf<Owner>(), property.name),
            gen = generator,
        ))
    }

    fun addOverride(override: FixtureOverride) {
        fixturesOverrides += override
    }

    fun getOverrides() = fixturesOverrides.toList()
    fun getOverriddenCollectionConfig() = collectionConfig
}