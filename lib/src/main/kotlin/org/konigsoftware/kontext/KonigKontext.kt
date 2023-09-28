package org.konigsoftware.kontext

import com.google.protobuf.Message
import io.grpc.Context
import kotlin.reflect.KClass
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod

class KonigKontext<KontextType>(
    internal val defaultValue: KontextType,
    internal val valueToBinary: (KontextType) -> ByteArray,
    internal val valueFromBinary: (ByteArray) -> KontextType,
) {
    internal val grpcContextKey = Context.key<KontextType>("konig-kontext-grpc-context")
    internal val grpcHeaderKey = KONIG_KONTEXT_GRPC_HEADER_KEY

    fun get(): KontextType = grpcContextKey.get() ?: defaultValue

    fun setValue(konigKontext: KontextType) = KonigKontextRunnable(konigKontext)

    companion object {
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <KontextType : Message> fromProtoMessage(messageClass: KClass<KontextType>): KonigKontext<KontextType> {
            val valueFromBinary =
                messageClass.functions.find { it.name == "parseFrom" && it.parameters.size == 1 && it.javaMethod?.parameterTypes?.first() == ByteArray::class.java }
                    ?: throw IllegalStateException("parseFrom function not present on com.google.protobuf.Message class")

            val valueToBinary =
                messageClass.functions.find { it.name == "toByteArray" && it.parameters.size == 1 }
                    ?: throw IllegalStateException("toByteArray function not present on com.google.protobuf.Message class")

            return KonigKontext(
                valueFromBinary.call("".toByteArray()) as? KontextType
                    ?: throw IllegalStateException("Unable to cast default value as KontextType"),
                { message -> valueToBinary.call(message) as ByteArray },
                { binary ->
                    valueFromBinary.call(binary) as? KontextType
                        ?: throw IllegalStateException("Unable to cast parsed binary as KontextType")
                })
        }

        @Suppress("UNCHECKED_CAST")
        @JvmName("fromProtoMessageJava")
        @JvmStatic
        fun <KontextType : Message> fromProtoMessage(messageClass: Class<KontextType>): KonigKontext<KontextType> {
            val valueFromBinary =
                runCatching { messageClass.getDeclaredMethod("parseFrom", ByteArray::class.java) }.getOrNull()
                    ?: throw IllegalStateException("parseFrom function not present on com.google.protobuf.Message class")

            val valueToBinary =
                runCatching { messageClass.getMethod("toByteArray") }.getOrNull()
                    ?: throw IllegalStateException("toByteArray function not present on com.google.protobuf.Message class")

            return KonigKontext(
                valueFromBinary.invoke(null, "".toByteArray()) as? KontextType
                    ?: throw IllegalStateException("Unable to cast default value as KontextType"),
                { message -> valueToBinary.invoke(message) as ByteArray },
                { binary ->
                    valueFromBinary.invoke(null, binary) as? KontextType
                        ?: throw IllegalStateException("Unable to cast parsed binary as KontextType")
                })
        }
    }

    inner class KonigKontextRunnable(private val konigKontext: KontextType) {
        @JvmName("run")
        fun runJava(r: Runnable) {
            Context.current().withValue(grpcContextKey, konigKontext).run(r)
        }

        internal inline fun <T> runKotlin(block: () -> T): T {
            val context = Context.current().withValue(grpcContextKey, konigKontext)
            val oldContext: Context = context.attach()
            return try {
                block()
            } finally {
                context.detach(oldContext)
            }
        }
    }
}

suspend fun <T, ContextType> withKonigKontext(
    konigKontextRunnable: KonigKontext<ContextType>.KonigKontextRunnable,
    block: suspend () -> T
): T = konigKontextRunnable.runKotlin { block() }
