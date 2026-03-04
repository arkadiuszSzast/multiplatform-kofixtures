package co.kofixtures.ksp.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.Modifier
import java.io.Writer

class RegistryFunctionGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {

    fun generate(
        objectDecl: KSClassDeclaration,
        classes: List<KSClassDeclaration>,
        originatingFiles: List<KSFile>,
        moduleName: String? = null,
    ) {
        val objectName = objectDecl.simpleName.asString()
        val packageName = objectDecl.packageName.asString()

        val ordered = orderClasses(classes)
        val hasSealedClasses = ordered.any { Modifier.SEALED in it.modifiers }

        val file = codeGenerator.createNewFile(
            Dependencies(false, *originatingFiles.toTypedArray()),
            packageName,
            "${objectName}Generated",
        )

        val valName = moduleName ?: objectName.replaceFirstChar { it.lowercase() }

        file.bufferedWriter().use { writer ->
            writer.write("@file:Suppress(\"UNCHECKED_CAST\")\n")
            writer.write("\n")
            writer.write("package $packageName\n")
            writer.write("\n")
            writer.write("import co.kofixtures.core.FixtureModule\n")
            writer.write("import co.kofixtures.core.Generator\n")
            writer.write("import co.kofixtures.core.fixtureModule\n")
            writer.write("import co.kofixtures.core.register\n")
            if (hasSealedClasses) {
                writer.write("import co.kofixtures.core.generatorFor\n")
                writer.write("import kotlin.reflect.typeOf\n")
            }
            writer.write("\n")
            writer.write("val $valName: FixtureModule = fixtureModule {\n")
            for (klass in ordered) {
                generateClass(klass, writer)
            }
            writer.write("}\n")
        }
    }

    // ── ordering ──────────────────────────────────────────────────────────────

    /**
     * DFS ordering: sealed subtypes before their parent.
     * This ensures that when the sealed parent's generator calls `generatorFor<Subtype>()`,
     * the subtype is already registered in the registry.
     */
    private fun orderClasses(classes: List<KSClassDeclaration>): List<KSClassDeclaration> {
        val result = mutableListOf<KSClassDeclaration>()
        val processed = mutableSetOf<String>()
        for (klass in classes) {
            val qn = klass.qualifiedName?.asString() ?: continue
            if (qn in processed) continue
            if (Modifier.SEALED in klass.modifiers) {
                collectSealedDfs(klass, result, processed)
            } else {
                result.add(klass)
                processed.add(qn)
            }
        }
        return result
    }

    private fun collectSealedDfs(
        klass: KSClassDeclaration,
        result: MutableList<KSClassDeclaration>,
        processed: MutableSet<String>,
    ) {
        for (subtype in klass.getSealedSubclasses()) {
            collectSealedDfs(subtype, result, processed)
        }
        val qn = klass.qualifiedName?.asString() ?: return
        if (qn !in processed) {
            result.add(klass)
            processed.add(qn)
        }
    }

    // ── code generation ───────────────────────────────────────────────────────

    private fun generateClass(klass: KSClassDeclaration, writer: Writer) {
        when {
            klass.classKind == ClassKind.OBJECT -> generateObject(klass, writer)
            klass.classKind == ClassKind.ENUM_CLASS -> generateEnum(klass, writer)
            Modifier.SEALED in klass.modifiers -> generateSealed(klass, writer)
            else -> generateDataOrClass(klass, writer)
        }
    }

    private fun generateObject(klass: KSClassDeclaration, writer: Writer) {
        val fqn = klass.qualifiedName!!.asString()
        writer.write("    register<$fqn> { Generator { _ -> $fqn } }\n")
    }

    private fun generateEnum(klass: KSClassDeclaration, writer: Writer) {
        val fqn = klass.qualifiedName!!.asString()
        writer.write(
            "    register<$fqn> { Generator { random -> " +
                "$fqn.entries[random.nextInt($fqn.entries.size)] } }\n",
        )
    }

    private fun generateSealed(klass: KSClassDeclaration, writer: Writer) {
        val fqn = klass.qualifiedName!!.asString()
        val subtypes = klass.getSealedSubclasses().toList()
        if (subtypes.isEmpty()) {
            logger.warn("Sealed class $fqn has no subtypes — skipping register")
            return
        }
        // Use 'this' (implicit FactoryScope receiver) to avoid lambda parameter naming issues
        writer.write("    register<$fqn> {\n")
        writer.write("        val generators = listOf<Generator<$fqn>>(\n")
        for (sub in subtypes) {
            val subFqn = sub.qualifiedName!!.asString()
            writer.write(
                "            registry.generatorFor(typeOf<$subFqn>(), null, activeOverrides),\n",
            )
        }
        writer.write("        )\n")
        writer.write("        Generator { random ->\n")
        writer.write("            generators[random.nextInt(generators.size)].next(random)\n")
        writer.write("        }\n")
        writer.write("    }\n")
    }

    private fun generateDataOrClass(klass: KSClassDeclaration, writer: Writer) {
        val fqn = klass.qualifiedName!!.asString()
        val params = klass.primaryConstructor?.parameters ?: emptyList()

        if (params.isEmpty()) {
            writer.write("    register<$fqn> { Generator { _ -> $fqn() } }\n")
            return
        }

        // Use 'this' (implicit FactoryScope receiver) — avoids type-inference issues with named param
        writer.write("    register<$fqn> {\n")
        writer.write("        Generator { random ->\n")
        writer.write("            $fqn(\n")
        for (param in params) {
            val paramName = param.name!!.asString()
            writer.write("                $paramName = sample($fqn::$paramName, random),\n")
        }
        writer.write("            )\n")
        writer.write("        }\n")
        writer.write("    }\n")
    }
}
