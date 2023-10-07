import com.google.protobuf.gradle.*

group = "example.services.shared"

plugins {
    id("example.kotlin-library-conventions")
    id("com.google.protobuf").version("0.9.4")
}

sourceSets {
    main {
        java {
            // Point it at the generated protos
            srcDir("build/generated/source/proto/main/grpc")
            srcDir("build/generated/source/proto/main/grpckt")
            srcDir("build/generated/source/proto/main/java")
            srcDir("build/generated/source/proto/main/kotlin")
        }
    }
}

dependencies {
    // Generates Kotlin and Java code from the proto definitions.
    protobuf(files("../protobuf"))

    implementation("org.konigsoftware:konig-kontext:1.0.0")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.24.4"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.58.0"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.0:jdk8@jar"
        }
    }

    generateProtoTasks {
        all().forEach {
            // https://github.com/google/protobuf-gradle-plugin/issues/331#issuecomment-543333726
            it.doFirst {
                delete(it.outputs)
            }
            it.builtins {
                id("kotlin")
            }
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}
