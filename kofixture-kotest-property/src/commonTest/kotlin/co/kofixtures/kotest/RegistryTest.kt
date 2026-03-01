package co.kofixtures.kotest

import co.kofixtures.core.Generator
import co.kofixtures.core.buildRegistry
import co.kofixtures.core.register
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.comparables.shouldBeBetween
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlin.random.Random

class KotestTest : FreeSpec({

})