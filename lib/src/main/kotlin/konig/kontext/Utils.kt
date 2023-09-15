package konig.kontext

import com.google.protobuf.Message
import io.grpc.CallOptions
import io.grpc.Metadata
import io.grpc.Metadata.Key
import io.grpc.stub.AbstractStub
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.full.functions
import kotlinx.coroutines.withContext
import org.konigsoftware.konig.kontext.CoroutineKonigKontext

internal val GRPC_HEADER = Key.of("konig-kontext-bin", Metadata.BINARY_BYTE_MARSHALLER)
val CALL_OPTIONS_KEY: CallOptions.Key<Message> = CallOptions.Key.create("konig-kontext-call-option")
object CoroutineKonigKontextKey : CoroutineContext.Key<CoroutineKonigKontext<*>>

suspend inline fun <reified ServiceT : AbstractStub<ServiceT>, RespT> ServiceT.kall(crossinline fn: suspend ServiceT.() -> RespT): RespT {
    val konigKontextValue = coroutineContext[CoroutineKonigKontextKey]?.context

    val clientWithKonigKontextCallOption: ServiceT? = runCatching {
        ServiceT::class.functions.find { it.name == "withOption" }
            ?.call(this, CALL_OPTIONS_KEY, konigKontextValue) as? ServiceT
    }.getOrNull()

    return if (konigKontextValue != null && clientWithKonigKontextCallOption != null) clientWithKonigKontextCallOption.fn() else fn()
}

suspend fun <T, ContextType : Message> withKonigKontextContext(
    requestContext: ContextType,
    block: suspend () -> T
): T = withContext(
    coroutineContext.plus(CoroutineKonigKontext(requestContext))
) {
    block()
}