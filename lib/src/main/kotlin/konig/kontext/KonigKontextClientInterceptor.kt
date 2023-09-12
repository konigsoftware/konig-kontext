package org.konigsoftware.konig.kontext

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor

class KonigKontextClientInterceptor<ContextType>(private val konigKontext: KonigKontext<ContextType>) : ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT, RespT>?,
        callOptions: CallOptions?,
        next: Channel?
    ): ClientCall<ReqT, RespT> = object : SimpleForwardingClientCall<ReqT, RespT>(next?.newCall(method, callOptions)) {
        override fun start(responseListener: Listener<RespT>?, headers: Metadata?) {
            val konigKontextValue =
                callOptions?.getOption(konigKontext.CALL_OPTIONS_KEY) ?: konigKontext.default

            headers?.put(konigKontext.GRPC_HEADER, this@KonigKontextClientInterceptor.konigKontext.toBinary(konigKontextValue))

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