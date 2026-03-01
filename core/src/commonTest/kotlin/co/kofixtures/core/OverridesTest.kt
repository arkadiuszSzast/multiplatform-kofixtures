package co.kofixtures.core

import co.kofixtures.core.utils.Person
import co.kofixtures.core.utils.Project
import co.kofixtures.core.utils.gen
import co.kofixtures.core.utils.random
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class OverridesTest : FreeSpec({

    "generators" - {

        "can override generator" {
            val registry = buildRegistry {
                register<Int> { gen { 20 } }
                register<String> { gen { "Joe" } }
                register<Person> {
                    gen {
                        Person(
                            sample(Person::name, it),
                            sample(Person::age, it)
                        )
                    }
                }
            }
            registry.generator<Person> {
                override<Int> { gen { 18 } }
            }.next(random) shouldBe Person("Joe", 18)
        }

        "can override generator with tag" {
            val registry = buildRegistry {
                register<Int> { gen { 20 } }
                register<String>("name") { gen { "Joe" } }
                register<Person> {
                    gen {
                        Person(
                            sample(Person::name, it, "name"),
                            sample(Person::age, it)
                        )
                    }
                }
            }
            registry.generator<Person> {
                override<String> { gen { "Jane" } }
            }.next(random) shouldBe Person("Jane", 20)
        }

        "override by type overrides all matching fields" {
            val registry = buildRegistry {
                register<String> { gen { "field" } }
                register<Project> {
                    gen {
                        Project(
                            sample(Project::name, it),
                            sample(Project::description, it)
                        )
                    }
                }
            }
            registry.generator<Project> {
                override<String> { gen { "override" } }
            }.next(random) shouldBe Project("override", "override")
        }

        "can override single field generator" {
            val registry = buildRegistry {
                register<String> { gen { "field" } }
                register<Project> {
                    gen {
                        Project(
                            sample(Project::name, it),
                            sample(Project::description, it)
                        )
                    }
                }
            }
            registry.generator<Project> {
                override(Project::name) { gen { "override" } }
            }.next(random) shouldBe Project("override", "field")
        }
    }
})