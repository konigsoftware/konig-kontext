package com.konigsoftware.kontext

import io.grpc.Context
import io.grpc.kotlin.GrpcContextElement
import kotlinx.coroutines.withContext

class KonigKontext<KontextValue> private constructor(
    internal val konigKontextKey: KonigKontextKey<KontextValue>,
    internal val konigKontextValue: KontextValue
) {
    companion object {
        @JvmStatic
        fun <KontextValue> withValue(key: KonigKontextKey<KontextValue>, value: KontextValue): KonigKontext<KontextValue> =
            KonigKontext(key, value)

        @JvmStatic
        fun <KontextValue> getValue(key: KonigKontextKey<KontextValue>): KontextValue = key.grpcContextKey.get() ?: key.defaultValue
    }

    fun run(r: Runnable) {
        Context.current().withValue(konigKontextKey.grpcContextKey, konigKontextValue).run(r)
    }
}

suspend fun <T, KontextValue> withKonigKontext(
    konigKontext: KonigKontext<KontextValue>,
    block: suspend () -> T
): T = withContext(
    GrpcContextElement(
        Context.current().withValue(konigKontext.konigKontextKey.grpcContextKey, konigKontext.konigKontextValue)
    )
) {
    block()
}
