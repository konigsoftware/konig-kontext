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
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("maven-publish")

    `java-library`
}

repositories {
    mavenCentral()
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
        includeTestsMatching("org.konigsoftware.kontext.*IT")
        this.isFailOnNoMatchingTests = true
    }
}

tasks.build {
    tasks.getByName("integrationTest").enabled = false
}

tasks.check {
    tasks.getByName("integrationTest").enabled = false
}