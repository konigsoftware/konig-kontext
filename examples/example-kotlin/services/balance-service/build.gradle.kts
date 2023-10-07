group = "example.services.balance"

application {
    mainClass.set("example.services.balance.MainKt")
}

plugins {
    id("example.kotlin-application-conventions")
}

dependencies {
    implementation(project(":shared"))

    implementation("org.konigsoftware:konig-kontext:1.0.0")
}