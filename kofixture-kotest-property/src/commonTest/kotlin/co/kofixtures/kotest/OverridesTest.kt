package co.kofixtures.kotest

import co.kofixtures.core.buildRegistry
import co.kofixtures.core.override
import co.kofixtures.kotest.override
import co.kofixtures.kotest.utils.Project
import co.kofixtures.kotest.utils.projectModule
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.constant

class OverridesTest : FreeSpec({

    "should override by type" {
        val registry = buildRegistry { includes(projectModule) }

        registry.sample<Project> {
            override<String> { "override" }
        } shouldBe Project("override", "override")
    }

    "can override by property" {
        val registry = buildRegistry { includes(projectModule) }

        val project = registry.sample<Project> {
            override(Project::name) { "override" }
        }
        project.name shouldBe "override"
        project.description.shouldNotBe("override")
    }

    "can override by type using arb" {
        val registry = buildRegistry { includes(projectModule) }
        registry.sample<Project> {
            override<String> { Arb.constant("override") }
        } shouldBe Project("override", "override")
    }
})
