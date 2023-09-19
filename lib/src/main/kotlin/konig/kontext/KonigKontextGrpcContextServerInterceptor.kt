package konig.kontext

import com.google.protobuf.Message
import io.grpc.Context
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCall.Listener
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import kotlin.reflect.KClass
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod

internal class KonigKontextGrpcContextServerInterceptor<ContextType : Message>(private val contextClass: KClass<ContextType>) :
    ServerInterceptor {
    private val binaryParserFunction =
        contextClass.functions.find { it.name == "parseFrom" && it.parameters.size == 1 && it.javaMethod?.parameterTypes?.first() == ByteArray::class.java }
            ?: throw IllegalStateException("Could not find binary parser function for ContextType. Please ensure your KonigKontext type extends the com.google.protobuf.Message class")

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>?,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): Listener<ReqT> {
        val konigKontext = getKonigKontextFromGrpcHeaders(headers, contextClass, binaryParserFunction)

        return withGrpcContext(Context.current().extendKonigKontext(konigKontext)) {
            next.startCall(call, headers)
        }
    }
}
