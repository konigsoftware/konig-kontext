package konig.kontext

import com.google.protobuf.Message
import io.grpc.BindableService
import io.grpc.Context
import io.grpc.Metadata
import io.grpc.ServerBuilder
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.safeCast
import org.konigsoftware.konig.kontext.KonigKontextCoroutineServerInterceptor
import org.konigsoftware.konig.kontext.KonigKontextServer

internal fun <ContextType : Message> getKonigKontextFromGrpcHeaders(
    headers: Metadata,
    contextClass: KClass<ContextType>,
    binaryParserFunction: KFunction<*>
): ContextType {
    val konigKontextBinary = headers.get(KONIG_KONTEXT_GRPC_HEADER_KEY) ?: "".toByteArray()

    val parsedContextUntyped = binaryParserFunction.call(konigKontextBinary) ?: throw IllegalStateException(
        "Invalid KonigKontext binary. Unable to parse ${
            String(konigKontextBinary)
        } as ${contextClass.simpleName}"
    )

    return contextClass.safeCast(parsedContextUntyped)
        ?: throw IllegalStateException("Unable to cast parsed Konig Kontext message as ${contextClass.simpleName}")
}

internal inline fun <R> withGrpcContext(context: Context, action: () -> R): R {
    val oldContext: Context = context.attach()
    return try {
        action()
    } finally {
        context.detach(oldContext)
    }
}

internal fun <ContextType : Message> Context.extendKonigKontext(konigKontext: ContextType): Context {
    return withValue(KONIG_KONTEXT_GRPC_CONTEXT_KEY, konigKontext)
}
