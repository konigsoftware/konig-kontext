package org.konigsoftware.kontext

import io.grpc.Context

abstract class KonigKontextKey<KontextValue> {
    internal val grpcContextKey = Context.key<KontextValue>("konig-kontext-grpc-context")
    internal val grpcHeaderKey = KONIG_KONTEXT_GRPC_HEADER_KEY

    abstract fun valueToBinary(value: KontextValue): ByteArray
    abstract fun valueFromBinary(binaryValue: ByteArray): KontextValue
    abstract val defaultValue: KontextValue
}
