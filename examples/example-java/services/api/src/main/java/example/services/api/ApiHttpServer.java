package example.services.api;

import example.services.intermediary.GetBalanceRequest1;
import example.services.intermediary.IntermediaryServiceGrpc;
import example.services.shared.AuthContext;
import example.services.shared.GetBalanceResponseStatus;
import example.services.shared.GlobalContextKeys;
import org.konigsoftware.kontext.KonigKontext;
import spark.Spark;

public class ApiHttpServer {
    final int port;
    private final IntermediaryServiceGrpc.IntermediaryServiceBlockingStub intermediaryServiceClient;

    public ApiHttpServer(int port, IntermediaryServiceGrpc.IntermediaryServiceBlockingStub intermediaryServiceClient) {
        this.intermediaryServiceClient = intermediaryServiceClient;
        this.port = port;
    }

    public void start() {
        Spark.port(this.port);

        Spark.get("/:userId/balance", (request, response) -> {
            // Build auth context. In practice this would probably come from an auth token
            var authContext = AuthContext
                    .newBuilder()
                    .setCustomerId(request.headers("Customer-Id"))
                    .build();

            // Get the user id from the path parameters
            var userId = request.params(":userId");

            var responseBody = new StringBuilder();

            // Set the global AUTH_CONTEXT to the current authContext
            KonigKontext.withValue(GlobalContextKeys.AUTH_CONTEXT_KEY, authContext).run(() -> {
                // Build request. Note just the user id (not the customer id) is passed in the request
                var getBalanceRequest = GetBalanceRequest1.newBuilder().setUserId(userId).build();

                // Call intermediary service
                var getBalanceResponse = intermediaryServiceClient.getBalance(getBalanceRequest);

                if (getBalanceResponse.getStatus() == GetBalanceResponseStatus.GET_BALANCE_SUCCESS) {
                    responseBody.append("{\"balance\":\"").append(getBalanceResponse.getBalance()).append("\"}");
                    response.status(200);
                } else if (getBalanceResponse.getStatus() == GetBalanceResponseStatus.GET_BALANCE_NOT_FOUND) {
                    response.status(404);
                } else {
                    response.status(500);
                }
            });

            return responseBody.toString();
        });

        Spark.get("/api-health", ((request, response) -> {
            response.status(200);
            return "{\"ok\":true}";
        }));
    }
}