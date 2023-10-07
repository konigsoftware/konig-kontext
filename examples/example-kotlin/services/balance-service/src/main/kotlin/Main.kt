package example.services.balance

import example.services.shared.GlobalAuthContextKey
import io.grpc.ServerBuilder
import org.konigsoftware.kontext.KonigKontextServerInterceptor

fun main() {
    println("Starting Balance Service...")

    val server = ServerBuilder
        .forPort(50052)
        .addService(BalanceService())
        .intercept(KonigKontextServerInterceptor(GlobalAuthContextKey))
        .build()

    server.start()

    println("Started Balance Service on port: ${server.port}")

    server.awaitTermination()
}