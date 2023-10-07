package example.services.shared

import org.konigsoftware.kontext.KonigKontextKey
import org.konigsoftware.kontext.KonigKontextProtobufKey

object GlobalAuthContextKey : KonigKontextProtobufKey<AuthContext>(AuthContext::class)

object MyContextKey : KonigKontextKey<String>() {
    override val defaultValue: String = ""

    override fun valueFromBinary(binaryValue: ByteArray): String = String(binaryValue)

    override fun valueToBinary(value: String): ByteArray = value.toByteArray()
}