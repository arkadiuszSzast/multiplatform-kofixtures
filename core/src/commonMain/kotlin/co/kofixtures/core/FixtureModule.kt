package co.kofixtures.core

class FixtureModule internal constructor(
    internal val block: FixtureRegistryBuilder.() -> Unit,
)

fun fixtureModule(block: FixtureRegistryBuilder.() -> Unit): FixtureModule = FixtureModule(block)
