package co.kofixtures.core

import kotlin.reflect.KType

/**
 * Decomposed overrides propagated through the resolution graph.
 * Split once on entry so each lookup is O(1).
 */
class ActiveOverrides private constructor(
    private val byType: Map<KType, Generator<*>>,
    private val byName: Map<NamedOverrideKey, Generator<*>>,
    private val collectionConfig: CollectionConfig?
) {
    fun resolveType(type: KType): Generator<*>? = byType[type]
    fun resolveNamed(key: NamedOverrideKey): Generator<*>? = byName[key]

    companion object {

        fun from(overrideScope: OverrideScope): ActiveOverrides {
            val fixturesOverrides = overrideScope.getOverrides()
            val byType = mutableMapOf<KType, Generator<*>>()
            val byName = mutableMapOf<NamedOverrideKey, Generator<*>>()
            fixturesOverrides.forEach { override ->
                when (override) {
                    is FixtureOverride.TypeBased -> byType[override.type] = override.gen
                    is FixtureOverride.Named -> byName[override.key] = override.gen
                }
            }
            return ActiveOverrides(byType, byName, overrideScope.getOverriddenCollectionConfig())
        }
    }
}