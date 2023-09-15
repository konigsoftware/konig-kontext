package org.konigsoftware.konig.kontext

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.kotlin.AbstractCoroutineStub
import konig.kontext.CALL_OPTIONS_KEY
import konig.kontext.GRPC_HEADER

class KonigKontextClientInterceptor : ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT, RespT>?,
        callOptions: CallOptions?,
        next: Channel?
    ): ClientCall<ReqT, RespT> = object : SimpleForwardingClientCall<ReqT, RespT>(next?.newCall(method, callOptions)) {
        override fun start(responseListener: Listener<RespT>?, headers: Metadata?) {
            val konigKontextValue =
                callOptions?.getOption(CALL_OPTIONS_KEY)

            if (konigKontextValue != null) {
                headers?.put(GRPC_HEADER, konigKontextValue.toByteArray())
            }

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

fun <T : AbstractCoroutineStub<T>> T.withKonigKontextInterceptor(): T =
    withInterceptors(KonigKontextClientInterceptor())