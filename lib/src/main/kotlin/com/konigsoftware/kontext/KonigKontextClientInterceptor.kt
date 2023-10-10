package com.konigsoftware.kontext

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.stub.AbstractStub

/**
 * A [ClientInterceptor] subtype that will send the current KonigKontext to the corresponding server via gRPC headers.
 * See the [documentation here](https://github.com/konigsoftware/konig-kontext#2-client-side-setup).
 */
class KonigKontextClientInterceptor<KontextType>(private val konigKontextKey: KonigKontextKey<KontextType>) :
    ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT, RespT>?,
        callOptions: CallOptions?,
        next: Channel?
    ): ClientCall<ReqT, RespT> = object : SimpleForwardingClientCall<ReqT, RespT>(next?.newCall(method, callOptions)) {
        override fun start(responseListener: Listener<RespT>?, headers: Metadata?) {
            headers?.put(
                konigKontextKey.grpcHeaderKey,
                konigKontextKey.valueToBinary(KonigKontext.getValue(konigKontextKey))
            )

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

/**
 * Registers a [KonigKontextClientInterceptor] as an interceptor on this stub.
 */
fun <T : AbstractStub<T>, KontextType> T.withKonigKontextInterceptor(konigKontextKey: KonigKontextKey<KontextType>): T =
    withInterceptors(KonigKontextClientInterceptor(konigKontextKey))
