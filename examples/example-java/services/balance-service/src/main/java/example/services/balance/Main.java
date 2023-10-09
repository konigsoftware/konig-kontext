package example.services.balance;

import example.services.shared.GlobalContextKeys;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.konigsoftware.kontext.KonigKontextServerInterceptor;
import io.grpc.protobuf.services.HealthStatusManager;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Balance Service...");

        var balanceServer = ServerBuilder
                .forPort(50062)
                .addService(new BalanceService())
                // Add KonigKontextInterceptor (required for implementing KonigKontext)
                .intercept(new KonigKontextServerInterceptor<>(GlobalContextKeys.AUTH_CONTEXT_KEY))
                // These are added so that tests can ensure the service is running properly. They are not needed for implementing KonigKontext
                .addService(ProtoReflectionService.newInstance())
                .addService(new HealthStatusManager().getHealthService())
                .build();

        try {
            balanceServer.start();

            System.out.println("Started Balance Service on port: " + balanceServer.getPort());

            balanceServer.awaitTermination();
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
