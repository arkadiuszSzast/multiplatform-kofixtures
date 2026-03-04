package co.kofixtures.ksp.generator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier

class ClassCollector(private val resolver: Resolver) {

    fun collect(
        packages: List<String>,
        explicitClasses: List<KSClassDeclaration>,
    ): List<KSClassDeclaration> {
        val fromPackages = collectFromPackages(packages)
        val all = (fromPackages + explicitClasses).distinctBy { it.qualifiedName?.asString() }
        return all.filter { isTopLevelProcessable(it) }
    }

    private fun collectFromPackages(packages: List<String>): List<KSClassDeclaration> {
        val fromFiles = resolver.getAllFiles()
            .flatMap { file ->
                val pkg = file.packageName.asString()
                if (packages.any { pkg == it || pkg.startsWith("$it.") }) {
                    file.declarations.filterIsInstance<KSClassDeclaration>()
                } else {
                    emptySequence()
                }
            }

        @OptIn(KspExperimental::class)
        val fromClasspath = packages
            .asSequence()
            .flatMap { resolver.getDeclarationsFromPackage(it).filterIsInstance<KSClassDeclaration>() }

        return (fromFiles + fromClasspath).distinctBy { it.qualifiedName?.asString() }.toList()
    }

    private fun isTopLevelProcessable(klass: KSClassDeclaration): Boolean {
        val parent = klass.parentDeclaration as? KSClassDeclaration
        if (parent != null && Modifier.SEALED in parent.modifiers) return false

        return when (klass.classKind) {
            ClassKind.OBJECT -> true

            ClassKind.ENUM_CLASS -> true

            ClassKind.CLASS -> {
                val isAbstract = Modifier.ABSTRACT in klass.modifiers
                val isSealed = Modifier.SEALED in klass.modifiers
                !isAbstract || isSealed
            }

            ClassKind.INTERFACE -> Modifier.SEALED in klass.modifiers

            else -> false
        }
    }
}
