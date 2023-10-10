package com.konigsoftware.kontext

import io.grpc.Context
import io.grpc.kotlin.GrpcContextElement
import kotlinx.coroutines.withContext

/**
 * Key-value store for globally shared context between any number of gRPC microservices. Allows you to set and get a key-value pair
 * at any point in a request lifetime across your entire gRPC stack. See the setup requirements [here](https://github.com/konigsoftware/konig-kontext#setup) before
 * using.
 */
class KonigKontext<KontextValue> private constructor(
    internal val konigKontextKey: KonigKontextKey<KontextValue>,
    internal val konigKontextValue: KontextValue
) {
    companion object {
        /**
         * @param key key for the value you want to set. See [here](https://github.com/konigsoftware/konig-kontext#1-create-konigkontextkey)
         * for documentation on creating a valid [KonigKontextKey]
         * @param value globally accessible value keyed by [key]
         *
         * @return KonigKontext instance. See [run] (for Java) or [withKonigKontext] (for Kotlin) to run a lambda on the returned KonigKontext instance.
         */
        @JvmStatic
        fun <KontextValue> withValue(key: KonigKontextKey<KontextValue>, value: KontextValue): KonigKontext<KontextValue> =
            KonigKontext(key, value)

        /**
         * @param key key for the value you want to access
         *
         * @return value keyed by [key]. If the key is not set, the default value for the [key] is returned
         */
        @JvmStatic
        fun <KontextValue> getValue(key: KonigKontextKey<KontextValue>): KontextValue = key.grpcContextKey.get() ?: key.defaultValue
    }

    /**
     * Suggested for Java usage only. Calls the specified Runnable with a given KonigKontext. Any code inside the provided
     * Runnable, or any downstream RPC on any downstream service called from inside the Runnable, can access any
     * previously set value on the given KonigKontext instance.
     */
    fun run(r: Runnable) {
        Context.current().withValue(konigKontextKey.grpcContextKey, konigKontextValue).run(r)
    }
}

/**
 * For Kotlin usage only. Calls the specified suspending block with a given KonigKontext, suspends until it completes, and returns the result.
 * Any code inside the provided block (or any downstream RPC on any downstream service called from inside the block) can access any
 * value on the given KonigKontext instance.
 */
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
