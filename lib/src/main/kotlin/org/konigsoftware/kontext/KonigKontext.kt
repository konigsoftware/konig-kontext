package org.konigsoftware.kontext

import io.grpc.Context

class KonigKontext<KontextValue> private constructor(
    internal val konigKontextKey: KonigKontextKey<KontextValue>,
    internal val konigKontextValue: KontextValue
) {
    companion object {
        @JvmStatic
        fun <KontextValue> withValue(key: KonigKontextKey<KontextValue>, value: KontextValue): KonigKontext<KontextValue> =
            KonigKontext(key, value)

        @JvmStatic
        fun <KontextValue> getValue(key: KonigKontextKey<KontextValue>): KontextValue = key.grpcContextKey.get()
    }

    fun run(r: Runnable) {
        Context.current().withValue(konigKontextKey.grpcContextKey, konigKontextValue).run(r)
    }
}

suspend fun <T, KontextValue> withKonigKontext(
    konigKontext: KonigKontext<KontextValue>,
    block: suspend () -> T
): T {
    val context =
        Context.current().withValue(konigKontext.konigKontextKey.grpcContextKey, konigKontext.konigKontextValue)
    val oldContext: Context = context.attach()
    return try {
        block()
    } finally {
        context.detach(oldContext)
    }
}
