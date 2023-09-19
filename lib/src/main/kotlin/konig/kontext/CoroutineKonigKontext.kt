package org.konigsoftware.konig.kontext

import com.google.protobuf.Message
import konig.kontext.CoroutineKonigKontextKey
import kotlin.coroutines.AbstractCoroutineContextElement

internal class CoroutineKonigKontext<ContextType : Message>(val context: ContextType) :
    AbstractCoroutineContextElement(CoroutineKonigKontextKey)
