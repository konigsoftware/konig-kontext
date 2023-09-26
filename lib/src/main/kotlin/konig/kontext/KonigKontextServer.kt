package org.konigsoftware.konig.kontext

import com.google.protobuf.Message
import io.grpc.BindableService
import io.grpc.ServerBuilder
import konig.kontext.KONIG_KONTEXT_GRPC_CONTEXT_KEY
import konig.kontext.KonigKontextServerInterceptor
import kotlin.reflect.KClass

interface KonigKontextServer<ContextType : Message> {
    @Suppress("UNCHECKED_CAST")
    suspend fun konigKontext(): ContextType = KONIG_KONTEXT_GRPC_CONTEXT_KEY.get() as? ContextType?
        ?: throw IllegalStateException("Unable to obtain Konig Kontext. Please ensure your KonigKontext type extends the com.google.protobuf.Message class")
}

fun <ContextType : Message> ServerBuilder<*>.addKonigKontextServer(
    server: KonigKontextServer<ContextType>,
    contextClass: KClass<ContextType>
): ServerBuilder<*> {
    return this
        .intercept(KonigKontextServerInterceptor(contextClass))
        .addService(server as BindableService)
}