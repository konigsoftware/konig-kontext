package org.konigsoftware.kontext

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.stub.AbstractStub

class KonigKontextClientInterceptor<KontextType>(private val konigKontext: KonigKontext<KontextType>) : ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT, RespT>?,
        callOptions: CallOptions?,
        next: Channel?
    ): ClientCall<ReqT, RespT> = object : SimpleForwardingClientCall<ReqT, RespT>(next?.newCall(method, callOptions)) {
        override fun start(responseListener: Listener<RespT>?, headers: Metadata?) {
            headers?.put(konigKontext.grpcHeaderKey, konigKontext.valueToBinary(konigKontext.get()))

            super.start(
                object : SimpleForwardingClientCallListener<RespT>(responseListener) {
                    override fun onHeaders(headers: Metadata?) {
                        super.onHeaders(headers)
                    }
                },
                headers
            )
        }
    }
}

fun <T : AbstractStub<T>, KontextType> T.withKonigKontextInterceptor(konigKontext: KonigKontext<KontextType>): T =
    withInterceptors(KonigKontextClientInterceptor(konigKontext))
