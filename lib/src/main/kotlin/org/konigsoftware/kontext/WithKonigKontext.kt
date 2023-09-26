package org.konigsoftware.kontext

import com.google.protobuf.Message
import io.grpc.Context as GrpcContext

suspend fun <T, ContextType : Message> withKonigKontext(
    konigKontext: ContextType,
    block: suspend () -> T
): T = withGrpcContext(GrpcContext.current().extendKonigKontext(konigKontext)) {
    block()
}
