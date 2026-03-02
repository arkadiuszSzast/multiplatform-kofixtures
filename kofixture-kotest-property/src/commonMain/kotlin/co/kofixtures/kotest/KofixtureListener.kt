package co.kofixtures.kotest

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec

class KofixtureListener(
    private val spec: KofixtureTest,
) : TestListener {
    override suspend fun beforeSpec(spec: Spec) {
        KofixtureContext.buildFor(this.spec)
    }

    override suspend fun afterSpec(spec: Spec) {
        KofixtureContext.releaseFor(this.spec)
    }
}
