package co.kofixtures.core.utils

import co.kofixtures.core.Generator
import kotlin.random.Random

val random = Random(seed = 42)

fun <T> gen(value: T) = Generator<T> { _ -> value }
fun <T> gen(block: (Random) -> T) = Generator(block)

data class Person(val name: String, val age: Int)