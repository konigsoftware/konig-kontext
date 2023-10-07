group = "example.services.api"

application {
    mainClass.set("example.services.api.MainKt")
}

plugins {
    id("example.kotlin-application-conventions")
}

dependencies {
    implementation(project(":shared"))

    implementation("org.konigsoftware:konig-kontext:1.0.0")

    implementation("io.ktor:ktor-server-core:2.3.5")
    implementation("io.ktor:ktor-server-netty:2.3.5")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.18.0")
    implementation("org.apache.logging.log4j:log4j-core:2.18.0")
    implementation("org.apache.logging.log4j:log4j-api:2.18.0")
}