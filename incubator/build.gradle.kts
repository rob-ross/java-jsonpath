plugins {
    id("java")
}

group = "org.killeroonie"
version = "0.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // Jackson dependencies
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    // Optional: JSON Schema validation
    implementation("com.fasterxml.jackson.module:jackson-module-jsonSchema:2.15.2")
    // Optional: Java 8 date/time support
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
    // Add SLF4J API
    implementation("org.slf4j:slf4j-api:2.0.9")
    // Logging implementation
    implementation("ch.qos.logback:logback-classic:1.5.13")

    implementation("org.jetbrains:annotations:24.0.1")

}

tasks.test {
    useJUnitPlatform()
}