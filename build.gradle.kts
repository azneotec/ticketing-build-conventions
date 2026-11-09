// Root of the repo: forwards lifecycle tasks to the included plugins build,
// so `./gradlew build publishToMavenLocal` covers plugins and arch-rules together.
plugins {
    base
}

val plugins = gradle.includedBuild("plugins")

tasks.named("build") { dependsOn(plugins.task(":build")) }
tasks.named("check") { dependsOn(plugins.task(":check")) }
// No "clean" forwarding: this build uses the plugins build to configure arch-rules, so
// cleaning it in the same run deadlocks. Clean it separately: ./gradlew -p plugins clean

tasks.register("publishToMavenLocal") {
    group = "publishing"
    description = "Publishes the convention plugins and ticketing-arch-rules to ~/.m2."
    dependsOn(plugins.task(":publishToMavenLocal"))
}
