package example.services.intermediary

import example.services.balance.BalanceServiceGrpcKt.BalanceServiceCoroutineStub
import example.services.balance.getBalanceRequest2
import example.services.intermediary.IntermediaryServiceGrpcKt.IntermediaryServiceCoroutineImplBase

class IntermediaryService(private val balanceServiceClient: BalanceServiceCoroutineStub) :
    IntermediaryServiceCoroutineImplBase() {
    override suspend fun getBalance(request: GetBalanceRequest1): GetBalanceResponse1 {
        val balanceResponse = balanceServiceClient.getBalance(getBalanceRequest2 {
            userId = request.userId
        })

        return getBalanceResponse1 {
            status = balanceResponse.status
            balance = balanceResponse.balance
        }
    }
}
