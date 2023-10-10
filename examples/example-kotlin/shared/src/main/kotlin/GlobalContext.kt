package example.services.shared

import com.konigsoftware.kontext.KonigKontextKey
import com.konigsoftware.kontext.KonigKontextProtobufKey

object GlobalAuthContextKey : KonigKontextProtobufKey<AuthContext>(AuthContext::class)

object MyContextKey : KonigKontextKey<String>() {
    override val defaultValue: String = ""

    override fun valueFromBinary(binaryValue: ByteArray): String = String(binaryValue)

    override fun valueToBinary(value: String): ByteArray = value.toByteArray()
}