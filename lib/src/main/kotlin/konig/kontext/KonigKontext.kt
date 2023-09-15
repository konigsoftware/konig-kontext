package org.konigsoftware.konig.kontext

import io.grpc.CallOptions
import io.grpc.Metadata
import io.grpc.Metadata.Key
import io.grpc.stub.AbstractStub
import kotlin.coroutines.coroutineContext
import kotlin.reflect.full.functions

abstract class KonigKontext<ContextType> {
    internal abstract val default: ContextType

    internal abstract fun toBinary(konigKontext: ContextType): ByteArray

    internal abstract fun fromBinary(binary: ByteArray): ContextType

    internal val CALL_OPTIONS_KEY: CallOptions.Key<ContextType> = CallOptions.Key.create("konig-kontext-call-option")

    internal val GRPC_HEADER: Key<ByteArray> = Key.of("konig-kontext-bin", Metadata.BINARY_BYTE_MARSHALLER)
}

class KonigKontextServer<ContextType>(private val konigKontext: KonigKontext<ContextType>) {
    private suspend fun konigKontext(): ContextType =
        coroutineContext[CoroutineKonigKontext]?.getContext() ?: konigKontext.default

    val CALL_OPTIONS_KEY = konigKontext.CALL_OPTIONS_KEY

    suspend inline fun <reified ServiceT : AbstractStub<ServiceT>, RespT> ServiceT.call(crossinline fn: suspend ServiceT.() -> RespT): RespT {
        val konigKontextValue = coroutineContext[CoroutineKonigKontext]?.getContext<ContextType>()

        val serviceWithKonigKontextCallOption: ServiceT? = runCatching {
            ServiceT::class.functions.find { it.name == "withOption" }
                ?.call(this, CALL_OPTIONS_KEY, konigKontextValue) as ServiceT
        }.getOrNull()

        return if (serviceWithKonigKontextCallOption != null) serviceWithKonigKontextCallOption.fn() else fn()
    }
}