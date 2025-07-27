plugins {
    id("java")
}

group = "org.killeroonie"
version = "0.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_22
    targetCompatibility = JavaVersion.VERSION_22
}

dependencies {
    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Logging
    // SLF4J API
    implementation("org.slf4j:slf4j-api:2.0.9")
    // during development and testing, we need a concrete logger implementation
    testImplementation("ch.qos.logback:logback-classic:1.5.13")

    // Annotations
    implementation("org.jetbrains:annotations:24.0.1")

    // Incubator- specific
    // --------------------
    // Jackson dependencies
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    // Optional: JSON Schema validation
    implementation("com.fasterxml.jackson.module:jackson-module-jsonSchema:2.15.2")
    // Optional: Java 8 date/time support
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")

}

tasks.test {
    useJUnitPlatform()
    // Configure verbose test output
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        showExceptions = true
        showCauses = true
        showStackTraces = true

        // Set to show detailed information about test execution
        info.events("started", "passed", "skipped", "failed", "standard_out", "standard_error")
        debug.events("started", "passed", "skipped", "failed", "standard_out", "standard_error")

        // To see all output in real-time during the test execution
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

        // Output all lifecycle events
        lifecycle {
            events = setOf(
                org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT,
                org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
            )
        }

    }

}
