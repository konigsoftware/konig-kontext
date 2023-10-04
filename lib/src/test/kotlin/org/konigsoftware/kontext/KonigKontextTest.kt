package org.konigsoftware.kontext

import io.grpc.Context
import io.grpc.examples.helloworld.HelloRequest
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class KonigKontextTest {
    private object TestKonigKontextKey : KonigKontextProtobufKey<HelloRequest>(HelloRequest::class)

    @Test
    fun `Given KonigKontext, when calling withValue, then the correct key and value are set`() {
        val konigKontextValue = HelloRequest.newBuilder().setName("hi").build()

        val newKonigKontext = KonigKontext.withValue(TestKonigKontextKey, konigKontextValue)

        assertEquals(TestKonigKontextKey, newKonigKontext.konigKontextKey)
        assertEquals(konigKontextValue, newKonigKontext.konigKontextValue)
    }

    @Test
    fun `Given empty gRPC context, when calling getValue, then the default KonigKontext value is returned`() {
        val konigKontextValue = KonigKontext.getValue(TestKonigKontextKey)

        assertEquals(HelloRequest.getDefaultInstance(), konigKontextValue)
        assertEquals(TestKonigKontextKey.defaultValue, konigKontextValue)
    }

    @Test
    fun `Given populated gRPC context, when calling getValue, then the proper KonigKontext value is returned`() {
        val setKonigKontextValue = HelloRequest.newBuilder().setName("hi").build()

        Context.current().withValue(TestKonigKontextKey.grpcContextKey, setKonigKontextValue).run {
            val fetchedKonigKontextValue = KonigKontext.getValue(TestKonigKontextKey)

            assertEquals(setKonigKontextValue, fetchedKonigKontextValue)
        }
    }

    @Test
    fun `Given KonigKontext value set by run function, when calling getValue, then the proper KonigKontext value is returned`() {
        val setKonigKontextValue = HelloRequest.newBuilder().setName("hi").build()

        KonigKontext.withValue(TestKonigKontextKey, setKonigKontextValue).run(Runnable {
            val fetchedKonigKontextValue = KonigKontext.getValue(TestKonigKontextKey)

            assertEquals(setKonigKontextValue, fetchedKonigKontextValue)
        })
    }

    @Test
    fun `Given KonigKontext value set by withKonigKontext function, when calling getValue, then the proper KonigKontext value is returned`() = runBlocking {
        val setKonigKontextValue = HelloRequest.newBuilder().setName("hi").build()

        withKonigKontext(KonigKontext.withValue(TestKonigKontextKey, setKonigKontextValue)) {
            val fetchedKonigKontextValue = KonigKontext.getValue(TestKonigKontextKey)

            assertEquals(setKonigKontextValue, fetchedKonigKontextValue)
        }
    }
}
