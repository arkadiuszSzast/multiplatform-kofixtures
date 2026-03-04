package co.kofixtures.ksp

import co.kofixtures.ksp.generator.ClassCollector
import co.kofixtures.ksp.generator.RegistryFunctionGenerator
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

private const val ANNOTATION_FQN = "co.kofixtures.ksp.KoFixture"

class KoFixtureProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(ANNOTATION_FQN)
        val collector = ClassCollector(resolver)
        val generator = RegistryFunctionGenerator(codeGenerator, logger)

        symbols
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.OBJECT }
            .forEach { objectDecl ->
                processObject(objectDecl, collector, generator, resolver)
            }

        return emptyList()
    }

    private fun processObject(
        objectDecl: KSClassDeclaration,
        collector: ClassCollector,
        generator: RegistryFunctionGenerator,
        resolver: Resolver,
    ) {
        val annotation = objectDecl.annotations
            .firstOrNull { it.shortName.asString() == "KoFixture" }
            ?: return

        val packages = (
            annotation.arguments
                .firstOrNull { it.name?.asString() == "packages" }
                ?.value as? List<*>
            )
            ?.filterIsInstance<String>()
            ?: emptyList()

        val explicitClasses = (
            annotation.arguments
                .firstOrNull { it.name?.asString() == "classes" }
                ?.value as? List<*>
            )
            ?.filterIsInstance<KSType>()
            ?.mapNotNull { it.declaration as? KSClassDeclaration }
            ?: emptyList()

        val moduleName = (
            annotation.arguments
                .firstOrNull { it.name?.asString() == "moduleName" }
                ?.value as? String
            )
            ?.takeIf { it.isNotEmpty() }

        val classes = collector.collect(packages, explicitClasses)
            .filter { it.qualifiedName?.asString() != objectDecl.qualifiedName?.asString() }
        val originatingFiles = resolver.getAllFiles().toList()

        generator.generate(objectDecl, classes, originatingFiles, moduleName)
    }
}
