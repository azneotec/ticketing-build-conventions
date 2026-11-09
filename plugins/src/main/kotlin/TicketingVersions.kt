/** Library versions the convention plugins add to every project. Bump here, then release. */
object TicketingVersions {
    const val JUNIT = "6.1.3"
    const val ASSERTJ = "3.27.7"
    const val JACOCO = "0.8.16"
    const val GOOGLE_JAVA_FORMAT = "2.0.0"
    const val JAVA = 25

    /** Minimum line coverage enforced by `jacocoTestCoverageVerification` (part of `check`). */
    const val MIN_LINE_COVERAGE = "0.80"
}
