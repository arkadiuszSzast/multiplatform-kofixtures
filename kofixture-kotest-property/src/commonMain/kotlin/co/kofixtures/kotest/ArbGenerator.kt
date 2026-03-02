package co.kofixtures.kotest

import co.kofixtures.core.Generator
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.next
import kotlin.random.Random

class ArbGenerator<T>(
    private val arb: Arb<T>,
) : Generator<T> {
    override fun next(random: Random): T = arb.next(RandomSource.seeded(random.nextLong()))

    fun unwrap(): Arb<T> = arb
}

fun <T> Arb<T>.asGenerator(): Generator<T> = ArbGenerator(this)

fun <T> Generator<T>.asArb(): Arb<T> = when (this) {
    is ArbGenerator<T> -> this.unwrap()
    else -> arbitrary { rs -> this@asArb.next(rs.random) }
}
