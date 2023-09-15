package org.konigsoftware.konig.kontext

import com.google.protobuf.Message
import konig.kontext.CoroutineKonigKontextKey
import kotlin.coroutines.coroutineContext

interface KonigKontextServer<ContextType : Message> {
    @Suppress("UNCHECKED_CAST")
    suspend fun konigKontext(): ContextType = coroutineContext[CoroutineKonigKontextKey]?.context as? ContextType
        ?: throw IllegalStateException("Unable to obtain Konig Kontext. Please ensure your KonigKontext type extends the com.google.protobuf.Message class")
}
