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

}

tasks.test {
    useJUnitPlatform()
}