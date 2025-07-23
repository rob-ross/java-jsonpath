
plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

group = "org.killeroonie" // Change this to your group ID
version = "0.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    // Add other dependencies as needed
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Java JsonPath")
                description.set("A Java implementation of RFC 9535 (JSONPath)")
                url.set("https://github.com/rob-ross/java-jsonpath")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/license/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("killeroonie")
                        name.set("Robert L. Ross")
                        email.set("rob.ross@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/rob-ross/java-jsonpath.git")
                    developerConnection.set("scm:git:ssh://github.com/rob-ross/java-jsonpath.git")
                    url.set("https://github.com/rob-ross/java-jsonpath")
                }
            }
        }
    }
}

// Configure signing for release builds
signing {
    sign(publishing.publications["mavenJava"])
}

// Configure Nexus publishing
nexusPublishing {
    repositories {
        sonatype {
            // For first-time users: You'll need to set up a Sonatype account
            // nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            // snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}