package org.konigsoftware.kontext

import io.grpc.examples.helloworld.HelloRequest
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

internal class KonigKontextProtobufKeyTest {
    private object KotlinInitializedTestKey : KonigKontextProtobufKey<HelloRequest>(HelloRequest::class)
    private val javaInitializedTestKey = KonigKontextProtobufKey.fromJavaClass(HelloRequest::class.java)

    @Test
    fun `Given KonigKontextProtobufKey instance, when accessing the defaultValue, then the proper default value is returned`() {
        assertEquals(HelloRequest.parseFrom("".toByteArray()), KotlinInitializedTestKey.defaultValue)
        assertEquals(HelloRequest.parseFrom("".toByteArray()), javaInitializedTestKey.defaultValue)
        assertEquals(HelloRequest.getDefaultInstance(), KotlinInitializedTestKey.defaultValue)
        assertEquals(HelloRequest.getDefaultInstance(), javaInitializedTestKey.defaultValue)
        assertEquals(KotlinInitializedTestKey.defaultValue, javaInitializedTestKey.defaultValue)
    }

    @Test
    fun `Given KonigKontextProtobufKey instance, when calling valueFromBinary, then the correct value is returned`() {
        val exampleValue1 = HelloRequest.newBuilder().setName("asdf_1234").build()
        val exampleValue2 = HelloRequest.newBuilder().setName("Hello asdf koj3ij0s9u09 kjr3osdf j").build()
        val exampleValue3 = HelloRequest.newBuilder().setName("lksjdf -i3r- 093 89 ] *** 342IJOD ").build()

        assertEquals(exampleValue1, KotlinInitializedTestKey.valueFromBinary(exampleValue1.toByteArray()))
        assertEquals(exampleValue1, javaInitializedTestKey.valueFromBinary(exampleValue1.toByteArray()))

        assertEquals(exampleValue2, KotlinInitializedTestKey.valueFromBinary(exampleValue2.toByteArray()))
        assertEquals(exampleValue2, javaInitializedTestKey.valueFromBinary(exampleValue2.toByteArray()))

        assertEquals(exampleValue3, KotlinInitializedTestKey.valueFromBinary(exampleValue3.toByteArray()))
        assertEquals(exampleValue3, javaInitializedTestKey.valueFromBinary(exampleValue3.toByteArray()))

        assertEquals(KotlinInitializedTestKey.defaultValue, KotlinInitializedTestKey.valueFromBinary(KotlinInitializedTestKey.defaultValue.toByteArray()))
        assertEquals(javaInitializedTestKey.defaultValue, javaInitializedTestKey.valueFromBinary(javaInitializedTestKey.defaultValue.toByteArray()))
    }

    @Test
    fun `Given KonigKontextProtobufKey instance, when calling valueToBinary, then the correct binary value is returned`() {
        val exampleValue1 = HelloRequest.newBuilder().setName("asdf_1234").build()
        val exampleValue2 = HelloRequest.newBuilder().setName("Hello asdf koj3ij0s9u09 kjr3osdf j").build()
        val exampleValue3 = HelloRequest.newBuilder().setName("lksjdf -i3r- 093 89 ] *** 342IJOD ").build()

        assertEquals(exampleValue1.toByteArray(), KotlinInitializedTestKey.valueToBinary(exampleValue1))
        assertEquals(exampleValue1.toByteArray(), javaInitializedTestKey.valueToBinary(exampleValue1))

        assertEquals(exampleValue2.toByteArray(), KotlinInitializedTestKey.valueToBinary(exampleValue2))
        assertEquals(exampleValue2.toByteArray(), javaInitializedTestKey.valueToBinary(exampleValue2))

        assertEquals(exampleValue3.toByteArray(), KotlinInitializedTestKey.valueToBinary(exampleValue3))
        assertEquals(exampleValue3.toByteArray(), javaInitializedTestKey.valueToBinary(exampleValue3))

        assertEquals(KotlinInitializedTestKey.defaultValue.toByteArray(), KotlinInitializedTestKey.valueToBinary(KotlinInitializedTestKey.defaultValue))
        assertEquals(javaInitializedTestKey.defaultValue.toByteArray(), javaInitializedTestKey.valueToBinary(javaInitializedTestKey.defaultValue))
    }

    private fun assertEquals(a: ByteArray, b: ByteArray) = assert(a.contentEquals(b))
}
