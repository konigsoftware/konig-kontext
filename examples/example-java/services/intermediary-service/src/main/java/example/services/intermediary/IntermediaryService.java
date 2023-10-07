package example.services.intermediary;

import example.services.balance.BalanceServiceGrpc;
import example.services.balance.GetBalanceRequest2;
import io.grpc.stub.StreamObserver;

public class IntermediaryService extends IntermediaryServiceGrpc.IntermediaryServiceImplBase {
    private final BalanceServiceGrpc.BalanceServiceBlockingStub balanceServiceClient;

    public IntermediaryService(BalanceServiceGrpc.BalanceServiceBlockingStub balanceServiceClient) {
        this.balanceServiceClient = balanceServiceClient;
    }

    @Override
    public void getBalance(GetBalanceRequest1 request, StreamObserver<GetBalanceResponse1> responseObserver) {
        var balanceResponse = balanceServiceClient.getBalance(
                GetBalanceRequest2.newBuilder()
                        .setUserId(request.getUserId())
                        .build()
        );

        responseObserver.onNext(GetBalanceResponse1.newBuilder()
                .setStatus(balanceResponse.getStatus())
                .setBalance(balanceResponse.getBalance()).build());
        responseObserver.onCompleted();
    }
}