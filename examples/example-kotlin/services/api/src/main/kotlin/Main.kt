package example.services.api

import example.services.intermediary.IntermediaryServiceGrpcKt.IntermediaryServiceCoroutineStub
import example.services.intermediary.getBalanceRequest1
import example.services.shared.GetBalanceResponseStatus.GET_BALANCE_NOT_FOUND
import example.services.shared.GetBalanceResponseStatus.GET_BALANCE_SUCCESS
import example.services.shared.GlobalAuthContextKey
import example.services.shared.authContext
import io.grpc.ManagedChannelBuilder
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.konigsoftware.kontext.KonigKontext
import org.konigsoftware.kontext.withKonigKontext
import org.konigsoftware.kontext.withKonigKontextInterceptor

fun main() {
    println("Starting API...")

    // Build intermediary-service client with KonigKontext interceptor
    val intermediaryServiceClient = IntermediaryServiceCoroutineStub(
        ManagedChannelBuilder.forTarget("0.0.0.0:50051").usePlaintext().build()
    ).withKonigKontextInterceptor(GlobalAuthContextKey)

    embeddedServer(Netty, port = 8080) {
        routing {
            get("/{userId}/balance") {
                // Build auth context. In practice this would probably come from an auth token

                val authContext = authContext {
                    customerId = call.request.headers["Customer-Id"] ?: ""
                }

                // Get the user id from the path parameters
                val userId = call.parameters["userId"] ?: ""

                // Set Konig Kontext
                val getBalanceResponse = withKonigKontext(KonigKontext.withValue(GlobalAuthContextKey, authContext)) {
                    // Call intermediary service
                    intermediaryServiceClient.getBalance(getBalanceRequest1 {
                        // Note just the user id (not the customer id) is passed in the request
                        this.userId = userId
                    })
                }

                return@get when (getBalanceResponse.status) {
                    GET_BALANCE_SUCCESS -> {
                        // Return the balance
                        call.respondText("{\"balance\":\"${getBalanceResponse.balance}\"}")
                    }
                    GET_BALANCE_NOT_FOUND -> call.respond(HttpStatusCode.NotFound)
                    else -> call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }.start(wait = true)
}

