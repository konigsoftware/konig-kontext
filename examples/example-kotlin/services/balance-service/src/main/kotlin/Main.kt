package example.services.balance

import example.services.shared.GlobalAuthContextKey
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.HealthStatusManager
import io.grpc.protobuf.services.ProtoReflectionService
import org.konigsoftware.kontext.KonigKontextServerInterceptor

fun main() {
    println("Starting Balance Service...")

    val server = ServerBuilder
        .forPort(50052)
        .addService(BalanceService())
        // Add KonigKontextInterceptor (required for implementing KonigKontext)
        .intercept(KonigKontextServerInterceptor(GlobalAuthContextKey))
        // These are added so that tests can ensure the service is running properly. They are not needed for implementing KonigKontext
        .addService(HealthStatusManager().healthService)
        .addService(ProtoReflectionService.newInstance())
        .build()

    server.start()

    println("Started Balance Service on port: ${server.port}")

    server.awaitTermination()
}