package org.konigsoftware.konig.kontext

import com.google.protobuf.Message
import io.grpc.BindableService
import io.grpc.Metadata
import io.grpc.ServerBuilder
import io.grpc.ServerCall
import io.grpc.kotlin.CoroutineContextServerInterceptor
import konig.kontext.GRPC_HEADER
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.safeCast

class KonigKontextServerInterceptor<ContextType : Message>(
    private val contextClass: KClass<ContextType>
) : CoroutineContextServerInterceptor() {
    private val binaryParserFunction =
        contextClass.functions.find { it.name == "parseFrom" && it.parameters.size == 1 && it.javaMethod?.parameterTypes?.first() == ByteArray::class.java }
            ?: throw IllegalStateException("Could not find binary parser function for ContextType. Please ensure your KonigKontext type extends the com.google.protobuf.Message class")

    override fun coroutineContext(call: ServerCall<*, *>, headers: Metadata): CoroutineContext {
        val konigKontextBinary = headers.get(GRPC_HEADER) ?: "".toByteArray()

        val parsedContextUntyped = binaryParserFunction.call(konigKontextBinary) ?: throw IllegalStateException(
            "Invalid KonigKontext binary. Unable to parse ${
                String(konigKontextBinary)
            } as ${contextClass.simpleName}"
        )

        val parsedContext =
            contextClass.safeCast(parsedContextUntyped)
                ?: throw IllegalStateException("Unable to cast parsed Konig Kontext message as ${contextClass.simpleName}")

        return CoroutineKonigKontext(parsedContext)
    }
}

inline fun <reified ContextType : Message> ServerBuilder<*>.addKonigKontextServer(server: KonigKontextServer<ContextType>): ServerBuilder<*> {
    return this.intercept(KonigKontextServerInterceptor(ContextType::class)).addService(server as BindableService)
}