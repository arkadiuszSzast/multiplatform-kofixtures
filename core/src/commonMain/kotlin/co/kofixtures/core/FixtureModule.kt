package co.kofixtures.core

class FixtureModule @PublishedApi internal constructor(
    @PublishedApi internal val block: FixtureRegistryBuilder.() -> Unit,
)

fun fixtureModule(block: FixtureRegistryBuilder.() -> Unit): FixtureModule = FixtureModule(block)