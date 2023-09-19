package org.konigsoftware.konig.kontext

import com.google.protobuf.Message
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.kotlin.CoroutineContextServerInterceptor
import konig.kontext.getKonigKontextFromGrpcHeaders
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod

internal class KonigKontextCoroutineServerInterceptor<ContextType : Message>(
    private val contextClass: KClass<ContextType>
) : CoroutineContextServerInterceptor() {
    private val binaryParserFunction =
        contextClass.functions.find { it.name == "parseFrom" && it.parameters.size == 1 && it.javaMethod?.parameterTypes?.first() == ByteArray::class.java }
            ?: throw IllegalStateException("Could not find binary parser function for ContextType. Please ensure your KonigKontext type extends the com.google.protobuf.Message class")

    override fun coroutineContext(call: ServerCall<*, *>, headers: Metadata): CoroutineContext {
        return CoroutineKonigKontext(getKonigKontextFromGrpcHeaders(headers, contextClass, binaryParserFunction))
    }
}

