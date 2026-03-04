package co.example.domain

import co.kofixtures.ksp.KoFixture

/**
 * Annotating this object with @KoFixture tells the KSP processor to generate
 * a `fun FixtureRegistryBuilder.DomainFixtures()` extension that registers
 * generators for every class in the listed packages.
 *
 * The generated file `DomainFixturesGenerated.kt` exposes a `domainFixtures: FixtureModule` val.
 * Use it in tests: `buildRegistry { includes(domainFixtures) }`.
 *
 * Registered types:
 *   - User          (data class  — constructor called with sampled field values)
 *   - Article       (data class  — constructor called with sampled field values)
 *   - Role          (enum class  — random constant via Role.values())
 *   - Status        (sealed class — random subtype; subtypes registered first)
 *   - Status.Draft, Status.Published  (objects — return the singleton)
 *   - Status.Rejected                 (data class — constructor with sampled reason)
 */
@KoFixture(packages = ["co.example.domain"])
object DomainFixtures
