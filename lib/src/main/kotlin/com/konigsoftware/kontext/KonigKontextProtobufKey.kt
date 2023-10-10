package com.konigsoftware.kontext

import com.google.protobuf.Message
import kotlin.reflect.KClass

/**
 * It can be a good practice to use a protobuf [Message] as the type of the value associated with your [KonigKontextKey].
 * Using a protobuf message allows you to have a global type definition for your context value, while also making it
 * easy to make updates to the type without shooting yourself in the foot. See further [documentation here](https://github.com/konigsoftware/konig-kontext#protobuf-message-based-type).
 */
open class KonigKontextProtobufKey<KontextValue : Message> private constructor(valueClass: Class<KontextValue>) :
    KonigKontextKey<KontextValue>() {
    constructor(valueClass: KClass<KontextValue>) : this(valueClass.java)

    companion object {
        @JvmStatic
        fun <KontextValue : Message> fromJavaClass(valueClass: Class<KontextValue>): KonigKontextProtobufKey<KontextValue> = KonigKontextProtobufKey(valueClass)
    }

    private val parseFromFunction =
        runCatching { valueClass.getDeclaredMethod("parseFrom", ByteArray::class.java) }.getOrNull()
            ?: throw IllegalStateException("parseFrom function not present on com.google.protobuf.Message class")

    private val toByteArrayFunction =
        runCatching { valueClass.getMethod("toByteArray") }.getOrNull()
            ?: throw IllegalStateException("toByteArray function not present on com.google.protobuf.Message class")

    override val defaultValue: KontextValue
        get() = valueFromBinary("".toByteArray())

    @Suppress("UNCHECKED_CAST")
    override fun valueFromBinary(binaryValue: ByteArray): KontextValue =
        parseFromFunction.invoke(null, binaryValue) as? KontextValue
            ?: throw IllegalStateException("Unable to cast parsed binary as KontextValue")

    override fun valueToBinary(value: KontextValue): ByteArray = toByteArrayFunction.invoke(value) as? ByteArray
        ?: throw IllegalStateException("Unable to cast encoded value as ByteArray")
}
