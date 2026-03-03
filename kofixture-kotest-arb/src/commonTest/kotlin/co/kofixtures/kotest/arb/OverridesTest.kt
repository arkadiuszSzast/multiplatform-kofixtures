package co.kofixtures.kotest.arb

import co.kofixtures.core.buildRegistry
import co.kofixtures.kotest.arb.override
import co.kofixtures.kotest.arb.utils.Project
import co.kofixtures.kotest.arb.utils.projectModule
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.constant

class OverridesTest : FreeSpec({

    "can override by property" {
        val registry = buildRegistry { includes(projectModule) }

        val project = registry.sample<Project> {
            override(Project::name) { Arb.constant("override") }
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
