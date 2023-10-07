package example.services.balance

import example.services.balance.BalanceServiceGrpcKt.BalanceServiceCoroutineImplBase
import example.services.shared.GetBalanceResponseStatus.GET_BALANCE_NOT_FOUND
import example.services.shared.GetBalanceResponseStatus.GET_BALANCE_SUCCESS
import example.services.shared.GlobalAuthContextKey
import org.konigsoftware.kontext.KonigKontext

class BalanceService : BalanceServiceCoroutineImplBase() {
    // In-memory database
    // Map of customer id and user id to balance in string format
    private val balanceDb: Map<Pair<String, String>, String> = mapOf(
        ("test_customer_id_1234" to "test_user_id_9876") to "3301.42",
        ("test_customer_id_0909" to "test_user_id_0000") to "19.87",
        ("test_customer_id_9281" to "test_user_id_0091") to "8310.921",
        ("test_customer_id_9032" to "test_user_id_3802") to "889.223",
        ("test_customer_id_1123" to "test_user_id_8731") to "11.19",
        ("test_customer_id_8013" to "test_user_id_3385") to "71239.91",
        ("test_customer_id_1859" to "test_user_id_8358") to "89134.12",
        ("test_customer_id_8302" to "test_user_id_9090") to "83.1",
        ("test_customer_id_0909" to "test_user_id_0091") to "1.91",
        ("test_customer_id_1234" to "test_user_id_8921") to "8310.92",
    )

    override suspend fun getBalance(request: GetBalanceRequest2): GetBalanceResponse2 {
        // Simply call KonigKontext.getValue(<key>) to fetch a KonigKontext value for the given key
        val authContext = KonigKontext.getValue(GlobalAuthContextKey)

        println("Getting balance for user: ${request.userId} and customer: ${authContext.customerId}")

        // Lookup balance in in-memory db
        val balance = balanceDb[authContext.customerId to request.userId]
            ?: return getBalanceResponse2 { status = GET_BALANCE_NOT_FOUND }

        return getBalanceResponse2 {
            status = GET_BALANCE_SUCCESS
            this.balance = balance
        }
    }
}
