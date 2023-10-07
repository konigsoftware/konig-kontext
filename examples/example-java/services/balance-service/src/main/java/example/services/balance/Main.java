package example.services.balance;

import example.services.shared.GlobalContextKeys;
import io.grpc.ServerBuilder;
import org.konigsoftware.kontext.KonigKontextServerInterceptor;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Balance Service...");

        var balanceServer = ServerBuilder
                .forPort(50062)
                .addService(new BalanceService())
                .intercept(new KonigKontextServerInterceptor<>(GlobalContextKeys.AUTH_CONTEXT_KEY))
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
