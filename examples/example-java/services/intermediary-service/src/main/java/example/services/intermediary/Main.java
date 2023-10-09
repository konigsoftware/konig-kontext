package example.services.intermediary;

import example.services.balance.BalanceServiceGrpc;
import example.services.shared.GlobalContextKeys;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.konigsoftware.kontext.KonigKontextClientInterceptor;
import org.konigsoftware.kontext.KonigKontextServerInterceptor;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Intermediary Service...");

        // Build balance-service client with KonigKontext interceptor
        var balanceServiceClient = BalanceServiceGrpc
                .newBlockingStub(ManagedChannelBuilder.forTarget("0.0.0.0:50062").usePlaintext().build())
                .withInterceptors(new KonigKontextClientInterceptor<>(GlobalContextKeys.AUTH_CONTEXT_KEY));

        var server = ServerBuilder
                .forPort(50061)
                .addService(new IntermediaryService(balanceServiceClient))
                // Add KonigKontextInterceptor (required for implementing KonigKontext)
                .intercept(new KonigKontextServerInterceptor<>(GlobalContextKeys.AUTH_CONTEXT_KEY))
                // These are added so that tests can ensure the service is running properly. They are not needed for implementing KonigKontext
                .addService(ProtoReflectionService.newInstance())
                .addService(new HealthStatusManager().getHealthService())
                .build();

        try {
            server.start();

            System.out.println("Started Intermediary Service on port: " + server.getPort());

            server.awaitTermination();
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
