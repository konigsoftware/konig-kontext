package example.services.intermediary

import example.services.balance.BalanceServiceGrpcKt.BalanceServiceCoroutineStub
import example.services.shared.GlobalAuthContextKey
import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.HealthStatusManager
import io.grpc.protobuf.services.ProtoReflectionService
import com.konigsoftware.kontext.KonigKontextServerInterceptor
import com.konigsoftware.kontext.withKonigKontextInterceptor

fun main() {
    println("Starting Intermediary Service...")

    val serviceCClient =
        BalanceServiceCoroutineStub(
            ManagedChannelBuilder.forTarget("0.0.0.0:50052").usePlaintext().build()
        ).withKonigKontextInterceptor(GlobalAuthContextKey)

    val server = ServerBuilder
        .forPort(50051)
        .addService(IntermediaryService(serviceCClient))
        // Add KonigKontextInterceptor (required for implementing KonigKontext)
        .intercept(KonigKontextServerInterceptor(GlobalAuthContextKey))
        // These are added so that tests can ensure the service is running properly. They are not needed for implementing KonigKontext
        .addService(HealthStatusManager().healthService)
        .addService(ProtoReflectionService.newInstance())
        .build()

    server.start()

    println("Started Intermediary Service on port: ${server.port}")

    server.awaitTermination()
}
