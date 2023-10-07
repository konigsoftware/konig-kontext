package example.services.balance;

import example.services.shared.GetBalanceResponseStatus;
import example.services.shared.GlobalContextKeys;
import io.grpc.stub.StreamObserver;
import org.apache.commons.math3.util.Pair;
import org.konigsoftware.kontext.KonigKontext;

import java.util.HashMap;
import java.util.Map;

public class BalanceService extends BalanceServiceGrpc.BalanceServiceImplBase {
    // In-memory database
    // Map of customer id and user id to balance in string format
    private final Map<Pair<String, String>, String> balanceDb = new HashMap<>();

    public BalanceService() {
        balanceDb.put(new Pair<>("test_customer_id_1234", "test_user_id_9876"), "3301.42");
        balanceDb.put(new Pair<>("test_customer_id_0909", "test_user_id_0000"), "19.87");
        balanceDb.put(new Pair<>("test_customer_id_9281", "test_user_id_0091"), "8310.921");
        balanceDb.put(new Pair<>("test_customer_id_9032", "test_user_id_3802"), "889.223");
        balanceDb.put(new Pair<>("test_customer_id_1123", "test_user_id_8731"), "11.19");
        balanceDb.put(new Pair<>("test_customer_id_8013", "test_user_id_3385"), "71239.91");
        balanceDb.put(new Pair<>("test_customer_id_1859", "test_user_id_8358"), "89134.12");
        balanceDb.put(new Pair<>("test_customer_id_8302", "test_user_id_9090"), "83.1");
        balanceDb.put(new Pair<>("test_customer_id_0909", "test_user_id_0091"), "1.91");
        balanceDb.put(new Pair<>("test_customer_id_1234", "test_user_id_8921"), "8310.921");
    }

    @Override
    public void getBalance(GetBalanceRequest2 request, StreamObserver<GetBalanceResponse2> responseObserver) {
        var authContext = KonigKontext.getValue(GlobalContextKeys.AUTH_CONTEXT_KEY);

        var balance = balanceDb.get(new Pair<>(authContext.getCustomerId(), request.getUserId()));

        System.out.println("Getting balance for user: " + request.getUserId() + " and customer: " + authContext.getCustomerId());

        GetBalanceResponse2 response;

        if (balance == null) {
            response = GetBalanceResponse2.newBuilder()
                    .setStatus(GetBalanceResponseStatus.GET_BALANCE_NOT_FOUND)
                    .build();
        } else {
            response = GetBalanceResponse2.newBuilder().
                    setStatus(GetBalanceResponseStatus.GET_BALANCE_SUCCESS).
                    setBalance(balance)
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}