package co.kofixtures.core

import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.typeOf

class FixtureRegistry internal constructor(
    internal val factories: Map<RegistryKey, (FactoryScope) -> Generator<*>>,
    val collectionConfig: CollectionConfig = CollectionConfig(),
) {
    private val nullableIndex: Map<Triple<KClassifier?, List<KTypeProjection>, String?>, KType> =
        factories.keys
            .filter { !it.type.isMarkedNullable }
            .associate { key ->
                Triple(key.type.classifier, key.type.arguments, key.tag) to key.type
            }

    inline fun <reified T> generator(
        tag: String? = null,
        noinline block: OverrideScope.() -> Unit = {
        },
    ): Generator<T> = resolve(typeOf<T>(), tag, block)

    @PublishedApi
    internal fun <T> resolve(
        type: KType,
        tag: String? = null,
        block: OverrideScope.() -> Unit = {},
    ): Generator<T> {
        val scope = OverrideScope(this).apply(block)
        val active = ActiveOverrides.from(scope)
        return resolve(type, tag, active)
    }

    inline fun <reified T> sample(
        random: Random = Random.Default,
        tag: String? = null,
        noinline block: OverrideScope.() -> Unit = {},
    ): T = resolve<T>(typeOf<T>(), tag, block).next(random)

    /**
     * Recursive type resolver.
     *
     * Resolution order:
     *   1. Type-based override   — active.byType[type]
     *   2. Registry with tag     — factories[type, tag]
     *   3. Registry primary      — factories[type, null]
     *   4. Nullable derivation   — T? → resolve(T) + 50/50 null
     *   5. Collection derivation — List/Set/Map
     *   6. Error
     *
     * Named overrides are checked in FactoryScope.get(property), not here.
     */
    @PublishedApi
    @Suppress("UNCHECKED_CAST")
    internal fun <T> resolve(
        type: KType,
        tag: String? = null,
        active: ActiveOverrides,
    ): Generator<T> {
        // 1. Type-based override
        active.resolveType(type)?.let { return it as Generator<T> }

        val scope = FactoryScope(this, active)

        // 2. Registry with tag
        if (tag != null) {
            factories[RegistryKey(type, tag)]?.let { return it(scope) as Generator<T> }
        }

        // 3. Registry primary
        factories[RegistryKey(type, null)]?.let { return it(scope) as Generator<T> }

        // 4. Nullable derivation
        if (type.isMarkedNullable) {
            val lookupKey = Triple(type.classifier, type.arguments, tag)
            val fallbackNoTagKey = Triple(type.classifier, type.arguments, null)
            (nullableIndex[lookupKey] ?: nullableIndex[fallbackNoTagKey])?.let { nonNullType ->
                val inner = resolve<T>(nonNullType, tag, active)
                return Generator { random ->
                    if (random.nextBoolean()) inner.next(random) else null as T
                }
            }
        }

        // 5. Collection derivation
        deriveCollection<T>(type, active)?.let { return it }

        // 6. Error
        val tagInfo = if (tag != null) " (tag=\"$tag\")" else ""
        error(
            "No generator registered for $type$tagInfo.\n" +
                "Registered keys: ${factories.keys.joinToString()}",
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> deriveCollection(
        type: KType,
        active: ActiveOverrides,
    ): Generator<T>? {
        val classifier = type.classifier as? KClass<*> ?: return null
        val args = type.arguments

        return when (classifier) {
            List::class, Collection::class, Iterable::class -> {
                val range = active.collectionConfig?.listSize ?: collectionConfig.listSize
                val elementType = args.firstOrNull()?.type ?: return null
                val elementGen = resolve<Any?>(elementType, active = active)
                Generator { random ->
                    List(random.nextInt(range.first, range.last + 1)) { elementGen.next(random) } as T
                }
            }

            Set::class -> {
                val range = active.collectionConfig?.setSize ?: collectionConfig.setSize
                val elementType = args.firstOrNull()?.type ?: return null
                val elementGen = resolve<Any?>(elementType, active = active)
                Generator { random ->
                    buildSet {
                        repeat(random.nextInt(range.first, range.last + 1)) { add(elementGen.next(random)) }
                    } as T
                }
            }

            Map::class -> {
                val range = active.collectionConfig?.mapSize ?: collectionConfig.mapSize
                val keyType = args.getOrNull(0)?.type ?: return null
                val valueType = args.getOrNull(1)?.type ?: return null
                val keyGen = resolve<Any?>(keyType, active = active)
                val valueGen = resolve<Any?>(valueType, active = active)
                Generator { random ->
                    buildMap {
                        repeat(random.nextInt(range.first, range.last + 1)) {
                            put(keyGen.next(random), valueGen.next(random))
                        }
                    } as T
                }
            }

            else -> {
                null
            }
        }
    }
}

class FixtureRegistryBuilder {
    @PublishedApi
    internal val factories =
        mutableMapOf<RegistryKey, FactoryScope.() -> Generator<*>>()

    var collectionConfig: CollectionConfig = CollectionConfig()

    fun collections(block: CollectionConfigBuilder.() -> Unit) {
        collectionConfig = CollectionConfigBuilder().apply(block).build()
    }

    fun includes(vararg modules: FixtureModule) {
        modules.forEach { it.block(this) }
    }

    fun <T> register(
        type: KType,
        tag: String? = null,
        factory: FactoryScope.() -> Generator<T>,
    ) {
        factories[RegistryKey(type, tag)] = factory
    }

    fun build(): FixtureRegistry = FixtureRegistry(factories.toMap(), collectionConfig)
}

inline fun <reified T> FixtureRegistryBuilder.register(
    tag: String? = null,
    noinline factory: FactoryScope.() -> Generator<T>,
) = register(typeOf<T>(), tag, factory)

fun buildRegistry(block: FixtureRegistryBuilder.() -> Unit): FixtureRegistry = FixtureRegistryBuilder().apply(block).build()
