package com.konigsoftware.kontext

import io.grpc.Context
import java.util.UUID
import io.grpc.Metadata.Key
import io.grpc.Metadata

/**
 * Key for indexing values in a KonigKontext instance. There is no way to access a KonigKontextKey's value without
 * having access to the key instance itself. Keys are generally stored in static fields.
 *
 * See the [documentation here](https://github.com/konigsoftware/konig-kontext#1-create-konigkontextkey) for an example implementation of this abstract class.
 */
abstract class KonigKontextKey<KontextValue> {
    private val keyName = "konig-kontext-grpc-context-${UUID.randomUUID()}"
    internal val grpcContextKey = Context.key<KontextValue>(keyName)
    internal val grpcHeaderKey = Key.of("$keyName-bin", Metadata.BINARY_BYTE_MARSHALLER)

    /**
     * Override this function to convert your [KontextValue] type into a binary ByteArray.
     */
    abstract fun valueToBinary(value: KontextValue): ByteArray

    /**
     * Override this function to convert a binary representation of your value into your [KontextValue] type
     */
    abstract fun valueFromBinary(binaryValue: ByteArray): KontextValue

    /**
     * If the value for this key is not set for the current KonigKontext instance, this is the default value that will be
     * returned instead.
     */
    abstract val defaultValue: KontextValue
}
