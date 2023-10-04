package org.konigsoftware.kontext

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

    private object TestProtobufKontextKey : KonigKontextProtobufKey<HelloRequest>(HelloRequest::class)

    private object TestCustomKontextKey : KonigKontextKey<String>() {
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

    @Test
    fun `Given KonigKontext intercepted server, when calling server RPC with empty gRPC header, then the default protobuf KonigKontext value is set on the GrpcContext`() {
        lateinit var capturedKonigKontext: HelloRequest

        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        capturedKonigKontext = TestProtobufKontextKey.grpcContextKey.get()

                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, KonigKontextServerInterceptor(TestProtobufKontextKey))).build()
                .start()
        )

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Create client stub with test client interceptor used to set headers. Set empty headers
        val blockingStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(Metadata()))

        blockingStub.sayHello(HelloRequest.getDefaultInstance())

        assertEquals(TestProtobufKontextKey.defaultValue, capturedKonigKontext)
    }

    @Test
    fun `Given KonigKontext intercepted server, when calling server RPC with empty gRPC header, then the default custom KonigKontext value is set on the GrpcContext`() {
        lateinit var capturedKonigKontext: String

        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        capturedKonigKontext = TestCustomKontextKey.grpcContextKey.get()

                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, KonigKontextServerInterceptor(TestCustomKontextKey))).build()
                .start()
        )

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Create client stub with test client interceptor used to set headers. Set empty headers
        val blockingStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(Metadata()))

        blockingStub.sayHello(HelloRequest.getDefaultInstance())

        assertEquals(TestCustomKontextKey.defaultValue, capturedKonigKontext)
        assertEquals("", capturedKonigKontext)
    }

    @Test
    fun `Given KonigKontext intercepted server, when calling server RPC with gRPC header set to default value, then the default protobuf KonigKontext value is set on the GrpcContext`() {
        lateinit var capturedKonigKontext: HelloRequest

        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        capturedKonigKontext = TestProtobufKontextKey.grpcContextKey.get()

                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, KonigKontextServerInterceptor(TestProtobufKontextKey))).build()
                .start()
        )

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Create client stub with test client interceptor used to set headers. Set default value
        val headers = Metadata()
        headers.put(TestProtobufKontextKey.grpcHeaderKey, TestProtobufKontextKey.valueToBinary(TestProtobufKontextKey.defaultValue))
        val blockingStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(headers))

        blockingStub.sayHello(HelloRequest.getDefaultInstance())

        assertEquals(TestProtobufKontextKey.defaultValue, capturedKonigKontext)
    }

    @Test
    fun `Given KonigKontext intercepted server, when calling server RPC with gRPC header set to default value, then the custom protobuf KonigKontext value is set on the GrpcContext`() {
        lateinit var capturedKonigKontext: String

        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        capturedKonigKontext = TestCustomKontextKey.grpcContextKey.get()

                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, KonigKontextServerInterceptor(TestCustomKontextKey))).build()
                .start()
        )

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Create client stub with test client interceptor used to set headers. Set default value
        val headers = Metadata()
        headers.put(TestCustomKontextKey.grpcHeaderKey, TestCustomKontextKey.valueToBinary(TestCustomKontextKey.defaultValue))
        val blockingStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(headers))

        blockingStub.sayHello(HelloRequest.getDefaultInstance())

        assertEquals(TestCustomKontextKey.defaultValue, capturedKonigKontext)
        assertEquals("", capturedKonigKontext)
    }

    @Test
    fun `Given KonigKontext intercepted server, when calling server RPC with gRPC header set, then the expected protobuf KonigKontext value is set on the GrpcContext`() {
        lateinit var capturedKonigKontext: HelloRequest

        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        capturedKonigKontext = TestProtobufKontextKey.grpcContextKey.get()

                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, KonigKontextServerInterceptor(TestProtobufKontextKey))).build()
                .start()
        )

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Create client stub with test client interceptor used to set headers. Set Konig Kontext
        val headers = Metadata()
        headers.put(
            TestProtobufKontextKey.grpcHeaderKey,
            TestProtobufKontextKey.valueToBinary(HelloRequest.newBuilder().setName("test_1234").build())
        )
        val blockingStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(headers))

        blockingStub.sayHello(HelloRequest.getDefaultInstance())

        assertEquals(HelloRequest.newBuilder().setName("test_1234").build(), capturedKonigKontext)
    }

    @Test
    fun `Given KonigKontext intercepted server, when calling server RPC with gRPC header set, then the expected custom KonigKontext value is set on the GrpcContext`() {
        lateinit var capturedKonigKontext: String

        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        capturedKonigKontext = TestCustomKontextKey.grpcContextKey.get()

                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, KonigKontextServerInterceptor(TestCustomKontextKey))).build()
                .start()
        )

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Create client stub with test client interceptor used to set headers. Set Konig Kontext
        val headers = Metadata()
        headers.put(
            TestCustomKontextKey.grpcHeaderKey,
            TestCustomKontextKey.valueToBinary("test_1234")
        )
        val blockingStub =
            GreeterGrpc.newBlockingStub(channel).withInterceptors(TestClientInterceptor(headers))

        blockingStub.sayHello(HelloRequest.getDefaultInstance())

        assertEquals("test_1234", capturedKonigKontext)
    }


    @Test
    fun `Given protobuf KonigKontext intercepted server, when calling server RPC with gRPC header set to malformed binary, then an error is thrown`() {
        // Create a fake in process server with KonigKontextServerInterceptor
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, KonigKontextServerInterceptor(TestProtobufKontextKey))).build()
                .start()
        )

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
}