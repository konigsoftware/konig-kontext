package konig.kontext

import com.google.protobuf.Message
import io.grpc.Context
import io.grpc.Metadata
import io.grpc.Metadata.Key
import kotlin.coroutines.CoroutineContext
import org.konigsoftware.konig.kontext.CoroutineKonigKontext

/**
 * gRPC header ([Metadata]) key whose value contains the binary version of the Konig Kontext
 */
internal val KONIG_KONTEXT_GRPC_HEADER_KEY = Key.of("konig-kontext-bin", Metadata.BINARY_BYTE_MARSHALLER)

/**
 * [io.grpc.Context] key for the current Konig Kontext
 */
internal val KONIG_KONTEXT_GRPC_CONTEXT_KEY: Context.Key<Message> =
    Context.key("konig-kontext-grpc-context")

/**
 * [kotlin.coroutines.CoroutineContext] key for the current [CoroutineKonigKontext]
 */
internal object CoroutineKonigKontextKey : CoroutineContext.Key<CoroutineKonigKontext<*>>