package konig.kontext

import com.google.protobuf.Message
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.withContext
import io.grpc.Context as GrpcContext

suspend fun <T, ContextType : Message> withKonigKontext(
    konigKontext: ContextType,
    block: suspend () -> T
): T = withGrpcContext(GrpcContext.current().extendKonigKontext(konigKontext)) {
    block()
}
