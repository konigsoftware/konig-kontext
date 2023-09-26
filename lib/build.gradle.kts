group = "org.konigsoftware"
version = "1.0.0"

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.konigsoftware"
            artifactId = "konig-kontext"
            version = "1.0.0"

            from(components["java"])
        }
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
    id("maven-publish")

    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.grpc:grpc-kotlin-stub:1.3.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
    implementation("com.google.protobuf:protobuf-java-util:3.24.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.12.7")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("io.grpc:grpc-testing:1.49.0")
    testImplementation("io.grpc:grpc-examples:0.15.0")
}

tasks.test {
    useJUnitPlatform()
}
