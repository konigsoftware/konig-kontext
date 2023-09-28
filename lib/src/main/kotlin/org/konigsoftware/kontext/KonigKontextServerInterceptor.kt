package org.konigsoftware.kontext

import io.grpc.Contexts
import io.grpc.Context as GrpcContext
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCall.Listener
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor

class KonigKontextServerInterceptor<KontextType>(private val konigKontextKey: KonigKontextKey<KontextType>) :
    ServerInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>?,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): Listener<ReqT> {
        val konigKontextBinary = headers.get(konigKontextKey.grpcHeaderKey) ?: konigKontextKey.valueToBinary(konigKontextKey.defaultValue)

        val konigKontextValue = runCatching {
            konigKontextKey.valueFromBinary(konigKontextBinary)
        }.getOrElse {
            // This should only ever happen given a bug in the valueFromBinary or valueToBinary function implementation.
            // A bug in the valueToBinary function could set malformed binary on the gRPC headers.
            // A bug in the valueFromBinary function could fail to parse properly formatted binary into the KontextType format.
            // However, both of these cases should be impossible given the requirements in the `init` block of KonigKontext
            // and the KonigKontext.setValue function
            throw IllegalStateException("Unable to parse KonigKontext value from binary. Ensure the KonigKontextKey valueFromBinary and valueToBinary functions are implemented properly")
        }

        val newGrpcContext = GrpcContext.current().withValue(konigKontextKey.grpcContextKey, konigKontextValue)

        return Contexts.interceptCall(newGrpcContext, call, headers, next);
    }
}
