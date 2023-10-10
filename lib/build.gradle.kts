plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("maven-publish")
    id("com.adarshr.test-logger") version "3.2.0"
    id("org.jetbrains.dokka") version "1.9.0"
    signing

    `java-library`
}

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
}

dependencies {
    implementation("io.grpc:grpc-kotlin-stub:1.4.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.10")
    implementation("com.google.protobuf:protobuf-java-util:3.24.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.grpc:grpc-testing:1.58.0")
    testImplementation("io.grpc:grpc-examples:0.15.0")
    testImplementation("com.squareup.okhttp3:okhttp:4.10.0")
    testImplementation("com.squareup.moshi:moshi-kotlin:1.14.0")
}

tasks.test {
    useJUnitPlatform()
    filter {
        includeTestsMatching("*Test")
        this.isFailOnNoMatchingTests = true
    }
}

tasks.register<Test>("integrationTest") {
    useJUnitPlatform()
    filter {
        includeTestsMatching("com.konigsoftware.kontext.*IT")
        this.isFailOnNoMatchingTests = true
    }
}

tasks.build {
    tasks.getByName("integrationTest").enabled = false
}

tasks.check {
    tasks.getByName("integrationTest").enabled = false
}

tasks.create<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    includeEmptyDirs = false
    from(tasks.named("dokkaHtml"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = rootProject.group as String
            artifactId = "konig-kontext"
            version = rootProject.version as String

            from(components["java"])

            artifact(tasks.named("javadocJar"))

            pom {
                url.set("https://github.com/konigsoftware/konig-kontext")

                name.set("Konig Kontext")
                description.set("A globally shared context for JVM based gRPC microservices")

                scm {
                    connection.set("scm:git:https://github.com/konigsoftware/konig-kontext.git")
                    developerConnection.set("scm:git:git@github.com:konigsoftware/konig-kontext.git")
                    url.set("https://github.com/konigsoftware/konig-kontext")
                }

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/license/mit/")
                    }
                }

                developers {
                    developer {
                        id.set("konigsoftware.com")
                        name.set("KonigKontext Contributors")
                        email.set("reidbuzby@gmail.com")
                        url.set("https://konigsoftware.com")
                        organization.set("Konig Software")
                        organizationUrl.set("https://konigsoftware.com")
                    }
                }
            }
        }
    }
}


signing {
    useInMemoryPgpKeys(System.getenv("GPG_PRIVATE_KEY"), System.getenv("GPG_PASSPHRASE"))
    sign(publishing.publications["mavenJava"])
}

tasks.withType<Sign> {
    onlyIf { System.getenv("GPG_PRIVATE_KEY") != null }
}
