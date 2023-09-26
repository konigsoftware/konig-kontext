package konig.kontext

import com.google.protobuf.Message
import io.grpc.Context

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
