package com.konigsoftware.kontext

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServerInterceptors
import io.grpc.StatusRuntimeException
import io.grpc.examples.helloworld.GreeterGrpc
import io.grpc.examples.helloworld.GreeterGrpc.GreeterImplBase
import io.grpc.examples.helloworld.HelloReply
import io.grpc.examples.helloworld.HelloRequest
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.stub.StreamObserver
import io.grpc.testing.GrpcCleanupRule
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KonigKontextServerInterceptorTest {
    @Rule
    val grpcCleanup = GrpcCleanupRule()

    @Test
    fun `Given KonigKontext intercepted server, when calling server RPC with empty gRPC header, then the default protobuf KonigKontext value is set on the GrpcContext`() {
        lateinit var capturedKonigKontext: HelloRequest

        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = createKonigKontextInterceptedInProcessServer(TestProtobufKontextKey) {
            capturedKonigKontext = it
        }

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())

        // Create client stub with test client interceptor used to set headers. Set empty headers
        val greeterServiceClientStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(Metadata()))

        greeterServiceClientStub.sayHello(HelloRequest.getDefaultInstance())

        assertEquals(TestProtobufKontextKey.defaultValue, capturedKonigKontext)
    }

    @Test
    fun `Given KonigKontext intercepted server, when calling server RPC with empty gRPC header, then the default custom KonigKontext value is set on the GrpcContext`() {
        lateinit var capturedKonigKontext: String

        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = createKonigKontextInterceptedInProcessServer(TestCustomKontextKey) {
            capturedKonigKontext = it
        }

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())

        // Create client stub with test client interceptor used to set headers. Set empty headers
        val greeterServiceClientStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(Metadata()))

        greeterServiceClientStub.sayHello(HelloRequest.getDefaultInstance())

        assertEquals(TestCustomKontextKey.defaultValue, capturedKonigKontext)
        assertEquals("", capturedKonigKontext)
    }

    @Test
    fun `Given KonigKontext intercepted server, when calling server RPC with gRPC header set to default value, then the default protobuf KonigKontext value is set on the GrpcContext`() {
        lateinit var capturedKonigKontext: HelloRequest

        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = createKonigKontextInterceptedInProcessServer(TestProtobufKontextKey) {
            capturedKonigKontext = it
        }

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())

        // Create client stub with test client interceptor used to set headers. Set default value
        val headers = Metadata()
        headers.put(TestProtobufKontextKey.grpcHeaderKey, TestProtobufKontextKey.valueToBinary(TestProtobufKontextKey.defaultValue))
        val greeterServiceClientStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(headers))

        greeterServiceClientStub.sayHello(HelloRequest.getDefaultInstance())

        assertEquals(TestProtobufKontextKey.defaultValue, capturedKonigKontext)
    }

    @Test
    fun `Given KonigKontext intercepted server, when calling server RPC with gRPC header set to default value, then the custom protobuf KonigKontext value is set on the GrpcContext`() {
        lateinit var capturedKonigKontext: String

        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = createKonigKontextInterceptedInProcessServer(TestCustomKontextKey) {
            capturedKonigKontext = it
        }

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())

        // Create client stub with test client interceptor used to set headers. Set default value
        val headers = Metadata()
        headers.put(TestCustomKontextKey.grpcHeaderKey, TestCustomKontextKey.valueToBinary(TestCustomKontextKey.defaultValue))
        val greeterServiceBlockingStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(headers))

        greeterServiceBlockingStub.sayHello(HelloRequest.getDefaultInstance())

        assertEquals(TestCustomKontextKey.defaultValue, capturedKonigKontext)
        assertEquals("", capturedKonigKontext)
    }

    @Test
    fun `Given KonigKontext intercepted server, when calling server RPC with a single gRPC header set, then the expected protobuf KonigKontext value is set on the GrpcContext`() {
        lateinit var capturedKonigKontext: HelloRequest

        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = createKonigKontextInterceptedInProcessServer(TestProtobufKontextKey) {
            capturedKonigKontext = it
        }

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())

        // Create client stub with test client interceptor used to set headers. Set Konig Kontext
        val headers = Metadata()
        headers.put(
            TestProtobufKontextKey.grpcHeaderKey,
            TestProtobufKontextKey.valueToBinary(HelloRequest.newBuilder().setName("test_1234").build())
        )
        val greeterServiceBlockingStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(headers))

        greeterServiceBlockingStub.sayHello(HelloRequest.getDefaultInstance())

        assertEquals(HelloRequest.newBuilder().setName("test_1234").build(), capturedKonigKontext)
    }

    @Test
    fun `Given KonigKontext intercepted server, when calling server RPC with multiple gRPC headers set, then all expected protobuf KonigKontext values are set on the GrpcContext`() {
        lateinit var capturedKonigKontext1: HelloRequest
        lateinit var capturedKonigKontext2: HelloRequest

        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = createKonigKontextInterceptedInProcessServer(TestProtobufKontextKey, TestProtobufKontextKey2, {
            capturedKonigKontext1 = it
        }, {
            capturedKonigKontext2 = it
        })

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())

        // Create client stub with test client interceptor used to set headers. Set Konig Kontext
        val headers = Metadata()
        headers.put(
            TestProtobufKontextKey.grpcHeaderKey,
            TestProtobufKontextKey.valueToBinary(HelloRequest.newBuilder().setName("test_1234").build())
        )
        headers.put(
            TestProtobufKontextKey2.grpcHeaderKey,
            TestProtobufKontextKey2.valueToBinary(HelloRequest.newBuilder().setName("other_test_9876").build())
        )
        val greeterServiceBlockingStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(headers))

        greeterServiceBlockingStub.sayHello(HelloRequest.getDefaultInstance())

        assertEquals(HelloRequest.newBuilder().setName("test_1234").build(), capturedKonigKontext1)
        assertEquals(HelloRequest.newBuilder().setName("other_test_9876").build(), capturedKonigKontext2)
    }

    @Test
    fun `Given KonigKontext intercepted server, when calling server RPC with a single gRPC header set, then the expected custom KonigKontext value is set on the GrpcContext`() {
        lateinit var capturedKonigKontext: String

        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = createKonigKontextInterceptedInProcessServer(TestCustomKontextKey) {
            capturedKonigKontext = it
        }

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Create client stub with test client interceptor used to set headers. Set Konig Kontext

        val headers = Metadata()
        headers.put(
            TestCustomKontextKey.grpcHeaderKey,
            TestCustomKontextKey.valueToBinary("test_1234")
        )
        val greeterServiceBlockingStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(headers))

        greeterServiceBlockingStub.sayHello(HelloRequest.getDefaultInstance())

        assertEquals("test_1234", capturedKonigKontext)
    }

    @Test
    fun `Given KonigKontext intercepted server, when calling server RPC with multiple gRPC headers set, then the expected custom KonigKontext values are set on the GrpcContext`() {
        lateinit var capturedKonigKontext: String
        lateinit var capturedKonigKontext2: String

        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = createKonigKontextInterceptedInProcessServer(TestCustomKontextKey, TestCustomKontextKey2, {
            capturedKonigKontext = it
        }, {
            capturedKonigKontext2 = it
        })

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Create client stub with test client interceptor used to set headers. Set Konig Kontext

        val headers = Metadata()
        headers.put(
            TestCustomKontextKey.grpcHeaderKey,
            TestCustomKontextKey.valueToBinary("test_1234")
        )
        headers.put(
            TestCustomKontextKey2.grpcHeaderKey,
            TestCustomKontextKey2.valueToBinary("other_test_9876")
        )
        val greeterServiceBlockingStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(headers))

        greeterServiceBlockingStub.sayHello(HelloRequest.getDefaultInstance())

        assertEquals("test_1234", capturedKonigKontext)
        assertEquals("other_test_9876", capturedKonigKontext2)
    }


    @Test
    fun `Given protobuf KonigKontext intercepted server, when calling server RPC with gRPC header set to malformed binary, then an error is thrown`() {
        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = createKonigKontextInterceptedInProcessServer(TestProtobufKontextKey)

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Create client stub with test client interceptor used to set headers. Set malformed Konig Kontext
        val headers = Metadata()
        headers.put(
            TestProtobufKontextKey.grpcHeaderKey,
            "malformed binary".toByteArray()
        )
        val blockingStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(headers))

        assertThrows<StatusRuntimeException> {
            blockingStub.sayHello(HelloRequest.getDefaultInstance())
        }

        // The actual cause of the StatusRuntimeException gets swallowed, so we cannot assert anything further.
        // The console output however indicates the proper error gets thrown here
    }

    private object TestProtobufKontextKey : KonigKontextProtobufKey<HelloRequest>(HelloRequest::class)
    private object TestProtobufKontextKey2 : KonigKontextProtobufKey<HelloRequest>(HelloRequest::class)

    private object TestCustomKontextKey : KonigKontextKey<String>() {
        override val defaultValue = ""

        override fun valueFromBinary(binaryValue: ByteArray): String = String(binaryValue)

        override fun valueToBinary(value: String): ByteArray = value.toByteArray()
    }

    private object TestCustomKontextKey2 : KonigKontextKey<String>() {
        override val defaultValue = ""

        override fun valueFromBinary(binaryValue: ByteArray): String = String(binaryValue)

        override fun valueToBinary(value: String): ByteArray = value.toByteArray()
    }

    // Test interceptor used to set the headers on an outgoing request
    private class TestClientInterceptor(private val headersToSet: Metadata) : ClientInterceptor {
        override fun <ReqT : Any?, RespT : Any?> interceptCall(
            method: MethodDescriptor<ReqT, RespT>?,
            callOptions: CallOptions?,
            next: Channel?
        ): ClientCall<ReqT, RespT> =
            object : SimpleForwardingClientCall<ReqT, RespT>(next?.newCall(method, callOptions)) {
                override fun start(responseListener: Listener<RespT>?, headers: Metadata?) {
                    headers?.merge(headersToSet)

                    super.start(
                        object : SimpleForwardingClientCallListener<RespT>(responseListener) {
                            override fun onHeaders(headers: Metadata?) {
                                super.onHeaders(headers)
                            }
                        },
                        headers
                    )
                }
            }
    }

    /**
     * Registers in process server for the GreeterImpl on the [grpcCleanup] rule.
     *
     * @param konigKontextKey KonigKontextKey that will be passed into the KonigKontextServerInterceptor registered on this server
     * @param setter function that takes in the captured KonigKontext value that can be used to set a value in a test for assertions
     *
     * @return [String] Registered server name
     */
    private fun <T> createKonigKontextInterceptedInProcessServer(konigKontextKey: KonigKontextKey<T>, setter: (T) -> Unit = {}): String {
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        val capturedKonigKontext = konigKontextKey.grpcContextKey.get()

                        setter(capturedKonigKontext)

                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, KonigKontextServerInterceptor(konigKontextKey))).build()
                .start()
        )

        return serverName
    }

    /**
     * Registers in process server for the GreeterImpl on the [grpcCleanup] rule with multiple KonigKontextKey's.
     *
     * @param konigKontextKey1 First KonigKontextKey that will be passed into the KonigKontextServerInterceptor registered on this server
     * @param konigKontextKey2 Second KonigKontextKey that will be passed into the KonigKontextServerInterceptor registered on this server
     * @param setter1 function that takes in the captured KonigKontext value for the [konigKontextKey1] that can be used to set a value in a test for assertions
     * @param setter2 function that takes in the captured KonigKontext value for the [konigKontextKey2] that can be used to set a value in a test for assertions
     *
     * @return [String] Registered server name
     */
    private fun <T> createKonigKontextInterceptedInProcessServer(konigKontextKey1: KonigKontextKey<T>, konigKontextKey2: KonigKontextKey<T>, setter1: (T) -> Unit = {}, setter2: (T) -> Unit = {}): String {
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        val capturedKonigKontext1 = konigKontextKey1.grpcContextKey.get()
                        val capturedKonigKontext2 = konigKontextKey2.grpcContextKey.get()

                        setter1(capturedKonigKontext1)
                        setter2(capturedKonigKontext2)

                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, KonigKontextServerInterceptor(konigKontextKey1, konigKontextKey2))).build()
                .start()
        )

        return serverName
    }

}