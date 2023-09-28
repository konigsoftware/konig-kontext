package org.konigsoftware.kontext

import io.grpc.Contexts
import io.grpc.Context as GrpcContext
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCall.Listener
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor

class KonigKontextServerInterceptor<KontextType>(private val konigKontext: KonigKontext<KontextType>) :
    ServerInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>?,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): Listener<ReqT> {
        val konigKontextBinary = headers.get(konigKontext.grpcHeaderKey) ?: konigKontext.valueToBinary(konigKontext.defaultValue)

        val konigKontextMessage = konigKontext.valueFromBinary(konigKontextBinary)

        val newGrpcContext = GrpcContext.current().withValue(konigKontext.grpcContextKey, konigKontextMessage)

        return Contexts.interceptCall(newGrpcContext, call, headers, next);
    }
}
