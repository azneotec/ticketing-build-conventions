plugins {
    id("ticketing.spring-boot-conventions")
}

val ticketingConventionsVersion = providers.gradleProperty("ticketingConventionsVersion").get()

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation("org.springframework.boot:spring-boot-starter")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.github.azneotec.ticketing:ticketing-arch-rules:$ticketingConventionsVersion")
}
