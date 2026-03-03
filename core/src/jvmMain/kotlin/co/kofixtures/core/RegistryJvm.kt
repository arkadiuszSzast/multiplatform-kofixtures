package co.kofixtures.core

import kotlin.reflect.KFunction
import kotlin.reflect.typeOf

inline fun <reified T> FixtureRegistryBuilder.registerOf(
    constructor: KFunction<T>,
    tag: String? = null,
) = register(typeOf<T>(), tag) {
    Generator { random ->
        val args = constructor.parameters.map { param ->
            val paramName = param.name
                ?: error(
                    "Constructor parameter at index ${param.index} in '${constructor.name}' has no name. " +
                        "Ensure the class is compiled with parameter names (default in Kotlin).",
                )
            val namedKey = NamedOverrideKey(typeOf<T>(), paramName)
            val gen: Generator<*> = activeOverrides.resolveNamed(namedKey)
                ?: registry.resolve<T>(param.type, null, activeOverrides)
            gen.next(random)
        }
        constructor.call(*args.toTypedArray())
    }
}
