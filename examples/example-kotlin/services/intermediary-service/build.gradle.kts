group = "example.services.intermediary"

application {
    mainClass.set("example.services.intermediary.MainKt")
}

plugins {
    id("example.kotlin-application-conventions")
}

dependencies {
    implementation(project(":shared"))

    implementation("com.konigsoftware:konig-kontext:1.0.0")
}