package example.services.intermediary

import example.services.balance.BalanceServiceGrpcKt.BalanceServiceCoroutineStub
import example.services.shared.GlobalAuthContextKey
import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import org.konigsoftware.kontext.KonigKontextServerInterceptor
import org.konigsoftware.kontext.withKonigKontextInterceptor

fun main() {
    println("Starting Intermediary Service...")

    val serviceCClient =
        BalanceServiceCoroutineStub(
            ManagedChannelBuilder.forTarget("0.0.0.0:50052").usePlaintext().build()
        ).withKonigKontextInterceptor(GlobalAuthContextKey)

    val server = ServerBuilder
        .forPort(50051)
        .addService(IntermediaryService(serviceCClient))
        .intercept(KonigKontextServerInterceptor(GlobalAuthContextKey))
        .build()

    server.start()

    println("Started Intermediary Service on port: ${server.port}")

    server.awaitTermination()
}
