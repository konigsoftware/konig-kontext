package org.konigsoftware.konig.kontext

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.kotlin.CoroutineContextServerInterceptor
import kotlin.coroutines.CoroutineContext

class KonigKontextServerInterceptor<ContextType>(private val konigKontext: KonigKontext<ContextType>) : CoroutineContextServerInterceptor() {
    override fun coroutineContext(call: ServerCall<*, *>, headers: Metadata): CoroutineContext {
        val coroutineKonigContext = CoroutineKonigKontext()

        val grpcKonigKontextBinary = headers.get(konigKontext.GRPC_HEADER) ?: run {
            coroutineKonigContext.setContext(konigKontext.default)

            return coroutineKonigContext
        }

        coroutineKonigContext.setContext(konigKontext.fromBinary(grpcKonigKontextBinary))

        return coroutineKonigContext
    }
}