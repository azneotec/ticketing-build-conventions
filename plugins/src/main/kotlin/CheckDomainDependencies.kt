import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Fails when the domain module's main classpath contains any external library that is not on the
 * allow-list. The domain must be plain Java: no Spring, JPA, Jackson, Kafka, gRPC ...
 *
 * Other projects of the same build (e.g. a shared kernel module) are allowed.
 */
@DisableCachingByDefault(because = "Cheap check over the resolved dependency graph")
abstract class CheckDomainDependencies : DefaultTask() {

    /** Resolved dependency graphs (compile and runtime classpath) to inspect. */
    @get:Input abstract val rootComponents: ListProperty<ResolvedComponentResult>

    /** Allowed coordinates: `group` (whole group) or `group:name` (one module). */
    @get:Input abstract val allowed: SetProperty<String>

    @TaskAction
    fun check() {
        val allow = allowed.get()
        val offenders = sortedSetOf<String>()
        rootComponents.get().forEach { root -> collect(root, allow, offenders, mutableSetOf()) }
        if (offenders.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Domain module must not depend on frameworks or libraries, but found:")
                    offenders.forEach { appendLine("  - $it") }
                    append("Move this code to an adapter, or, if the library is plain Java and ")
                    append("framework-free, add it to domainDependencyGuard.allowed with a reason.")
                }
            )
        }
    }

    private fun collect(
        component: ResolvedComponentResult,
        allow: Set<String>,
        offenders: MutableSet<String>,
        seen: MutableSet<ResolvedComponentResult>,
    ) {
        if (!seen.add(component)) return
        component.dependencies.filterIsInstance<ResolvedDependencyResult>().forEach { dep ->
            val selected = dep.selected
            val id = selected.id
            if (id is ModuleComponentIdentifier && id.group !in allow && "${id.group}:${id.module}" !in allow) {
                offenders += "${id.group}:${id.module}:${id.version}"
            }
            collect(selected, allow, offenders, seen)
        }
    }
}
