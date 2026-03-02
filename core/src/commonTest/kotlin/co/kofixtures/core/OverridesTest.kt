package co.kofixtures.core

import co.kofixtures.core.utils.Person
import co.kofixtures.core.utils.Project
import co.kofixtures.core.utils.ProjectAssignment
import co.kofixtures.core.utils.Team
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
                            sample(Person::age, it),
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
                            sample(Person::age, it),
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
                            sample(Project::description, it),
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
                            sample(Project::description, it),
                        )
                    }
                }
            }
            registry.generator<Project> {
                override(Project::name) { gen { "override" } }
            }.next(random) shouldBe Project("override", "field")
        }
    }

    "can override nested generator" - {
        val registry = buildRegistry {
            register<String> { gen { "field" } }
            register<Int> { gen { 30 } }
            register<Person> {
                gen {
                    Person(
                        sample(Person::name, it),
                        sample(Person::age, it),
                    )
                }
            }
            register<Project> {
                gen {
                    Project(
                        sample(Project::name, it),
                        sample(Project::description, it),
                    )
                }
            }
            register<ProjectAssignment> {
                gen {
                    ProjectAssignment(
                        sample(ProjectAssignment::project, it),
                        sample(ProjectAssignment::user, it),
                    )
                }
            }
        }

        registry.generator<ProjectAssignment> {
            override(Person::name) {
                gen { "Joe" }
            }
            override(Person::age) { gen { 20 } }
        }.next(random) shouldBe
            ProjectAssignment(
                project = Project("field", "field"),
                user = Person("Joe", 20),
            )
    }

    "when generator is not defined still can be provided in override without errors" {
        val registry = buildRegistry {
            register<String> { gen { "Joe" } }
            register<Person> {
                gen {
                    Person(
                        sample(Person::name, it),
                        sample(Person::age, it),
                    )
                }
            }
        }
        registry.generator<Person> {
            override(Person::age) { gen { 20 } }
        }.next(random) shouldBe Person("Joe", 20)
    }

    "can override generator by type with const value" {
        val registry = buildRegistry {
            register<Int> { gen { 20 } }
            register<String> { gen { "Joe" } }
            register<Project> {
                gen {
                    Project(
                        sample(Project::name, it),
                        sample(Project::description, it),
                    )
                }
            }
        }

        registry.generator<Project> {
            override<String> { "field" }
        }.next(random) shouldBe Project("field", "field")
    }

    "can override property generator with const value" {
        val registry = buildRegistry {
            register<Int> { gen { 20 } }
            register<String> { gen { "Joe" } }
            register<Project> {
                gen {
                    Project(
                        sample(Project::name, it),
                        sample(Project::description, it),
                    )
                }
            }
        }
        registry.generator<Project> {
            override(Project::name) { "field" }
        }.next(random) shouldBe Project("field", "Joe")
    }

    "can override nested generator with const value" {
        val registry = buildRegistry {
            register<String> { gen { "field" } }
            register<Int> { gen { 30 } }
            register<Person> {
                gen {
                    Person(
                        sample(Person::name, it),
                        sample(Person::age, it),
                    )
                }
            }
            register<Project> {
                gen {
                    Project(
                        sample(Project::name, it),
                        sample(Project::description, it),
                    )
                }
            }
            register<ProjectAssignment> {
                gen {
                    ProjectAssignment(
                        sample(ProjectAssignment::project, it),
                        sample(ProjectAssignment::user, it),
                    )
                }
            }
        }

        registry.generator<ProjectAssignment> {
            override(Person::name) { "Joe" }
            override(Person::age) { 20 }
        }.next(random) shouldBe ProjectAssignment(
            project = Project("field", "field"),
            user = Person("Joe", 20),
        )
    }

    "can override collection config" {
        var counter = 0
        val registry = buildRegistry {
            collections {
                listSize = 1..1
            }
            register<String> { gen { "item" } }
            register<Int> { gen { counter++ } }
            register<Person> {
                gen {
                    Person(
                        sample(Person::name, it),
                        sample(Person::age, it),
                    )
                }
            }
            register<Team> {
                gen {
                    Team(sample(Team::members, it))
                }
            }
        }
        registry.generator<Team> {
            collections { listSize = 2..2 }
        }.next(random).members.size shouldBe 2

        registry.generator<Set<Int>> {
            collections { setSize = 3..3 }
        }.next(random).size shouldBe 3

        registry.generator<Map<Int, Int>> {
            collections { mapSize = 4..4 }
        }.next(random).size shouldBe 4
    }
})
