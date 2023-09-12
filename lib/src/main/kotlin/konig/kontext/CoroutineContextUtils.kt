package org.konigsoftware.konig.kontext

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class CoroutineKonigKontext :
    AbstractCoroutineContextElement(CoroutineKonigKontext) {
    companion object Key : CoroutineContext.Key<CoroutineKonigKontext>

    private var context: Any? = null

    fun <ContextType> setContext(context: ContextType) {
        this.context = context
    }

    fun <ContextType> getContext(): ContextType = context as? ContextType ?: throw IllegalStateException("Illegal CoroutineKonigContext access before initialization. Please call setContext() before getContext()")
}