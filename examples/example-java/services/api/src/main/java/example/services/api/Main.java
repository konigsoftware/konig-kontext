package example.services.api;

import example.services.intermediary.IntermediaryServiceGrpc;
import example.services.shared.GlobalContextKeys;
import io.grpc.ManagedChannelBuilder;
import com.konigsoftware.kontext.KonigKontextClientInterceptor;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting API...");

        // Build intermediary-service client with KonigKontext interceptor
        var intermediaryServiceClient = IntermediaryServiceGrpc
                .newBlockingStub(ManagedChannelBuilder.forTarget("0.0.0.0:50061").usePlaintext().build())
                .withInterceptors(new KonigKontextClientInterceptor<>(GlobalContextKeys.AUTH_CONTEXT_KEY));

        var server = new ApiHttpServer(8081, intermediaryServiceClient);

        server.start();
    }
}
