package org.konigsoftware.kontext

import io.grpc.Context as GrpcContext
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCall.Listener
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.ServerInterceptors
import io.grpc.examples.helloworld.GreeterGrpc
import io.grpc.examples.helloworld.GreeterGrpc.GreeterImplBase
import io.grpc.examples.helloworld.HelloReply
import io.grpc.examples.helloworld.HelloRequest
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.stub.StreamObserver
import io.grpc.testing.GrpcCleanupRule
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class KonigKontextClientInterceptorTest {
    @Rule
    val grpcCleanup = GrpcCleanupRule()

    private object TestProtobufKontextKey : KonigKontextProtobufKey<HelloRequest>(HelloRequest::class)

    private object TestCustomKontextKey : KonigKontextKey<String>() {
        override val defaultValue = ""

        override fun valueFromBinary(binaryValue: ByteArray): String = String(binaryValue)

        override fun valueToBinary(value: String): ByteArray = value.toByteArray()
    }

    // Test interceptor used to assert the headers captured on an incoming request
    private class TestServerInterceptor : ServerInterceptor {
        var capturedMetadata: Metadata? = null

        override fun <ReqT : Any?, RespT : Any?> interceptCall(
            call: ServerCall<ReqT, RespT>?,
            headers: Metadata,
            next: ServerCallHandler<ReqT, RespT>
        ): Listener<ReqT> {
            capturedMetadata = headers

            return next.startCall(call, headers)
        }
    }

    @Test
    fun `Given downstream rpc, when calling rpc from KonigKontext intercepted client and the protobuf KonigKontext is empty, then the KonigKontextKey default value is included on request headers`() {
        val testServerInterceptor = TestServerInterceptor()

        // Create a fake in process server
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, testServerInterceptor)).build()
                .start()
        )

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Register KonigKontextClientInterceptor
        val blockingStub =
            GreeterGrpc.newBlockingStub(channel).withKonigKontextInterceptor(TestProtobufKontextKey)

        blockingStub.sayHello(HelloRequest.getDefaultInstance())

        assertNotNull(testServerInterceptor.capturedMetadata)
        assertNotNull(testServerInterceptor.capturedMetadata!!.get(TestProtobufKontextKey.grpcHeaderKey))
        assert(HelloRequest.getDefaultInstance().toByteArray().contentEquals(testServerInterceptor.capturedMetadata!!.get(TestProtobufKontextKey.grpcHeaderKey)))
        assert(TestProtobufKontextKey.valueToBinary(HelloRequest.getDefaultInstance()).contentEquals(testServerInterceptor.capturedMetadata!!.get(TestProtobufKontextKey.grpcHeaderKey)))
    }

    @Test
    fun `Given downstream rpc, when calling rpc from KonigKontext intercepted client and the custom KonigKontext is empty, then the KonigKontextKey default value is included on request headers`() {
        val testServerInterceptor = TestServerInterceptor()

        // Create a fake in process server
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, testServerInterceptor)).build()
                .start()
        )

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Register KonigKontextClientInterceptor
        val blockingStub =
            GreeterGrpc.newBlockingStub(channel).withKonigKontextInterceptor(TestCustomKontextKey)

        blockingStub.sayHello(HelloRequest.getDefaultInstance())

        assertNotNull(testServerInterceptor.capturedMetadata)
        assertNotNull(testServerInterceptor.capturedMetadata!!.get(TestCustomKontextKey.grpcHeaderKey))
        assert("".toByteArray().contentEquals(testServerInterceptor.capturedMetadata!!.get(TestCustomKontextKey.grpcHeaderKey)))
        assert(TestCustomKontextKey.valueToBinary("").contentEquals(testServerInterceptor.capturedMetadata!!.get(TestCustomKontextKey.grpcHeaderKey)))
    }

    @Test
    fun `Given downstream rpc, when calling rpc from KonigKontext intercepted client and the protobuf KonigKontext is set, then the KonigKontext is included on request headers`() {
        val testServerInterceptor = TestServerInterceptor()

        // Create a fake in process server
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, testServerInterceptor)).build()
                .start()
        )

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Register KonigKontextClientInterceptor
        val blockingStub =
            GreeterGrpc.newBlockingStub(channel).withKonigKontextInterceptor(TestProtobufKontextKey)

        // Build KonigKontext which has type HelloRequest
        val konigKontextValue = HelloRequest.newBuilder().setName("my_custom_field_1234").build()

        GrpcContext.current().withValue(TestProtobufKontextKey.grpcContextKey, konigKontextValue).run {
            blockingStub.sayHello(HelloRequest.getDefaultInstance())
        }

        assertNotNull(testServerInterceptor.capturedMetadata)
        assertNotNull(testServerInterceptor.capturedMetadata!!.get(TestProtobufKontextKey.grpcHeaderKey))
        assertEquals(konigKontextValue, HelloRequest.parseFrom(testServerInterceptor.capturedMetadata!!.get(TestProtobufKontextKey.grpcHeaderKey)))
        assertEquals(konigKontextValue, TestProtobufKontextKey.valueFromBinary(testServerInterceptor.capturedMetadata!!.get(TestProtobufKontextKey.grpcHeaderKey)!!))
    }

    @Test
    fun `Given downstream rpc, when calling rpc from KonigKontext intercepted client and the custom KonigKontext is set, then the KonigKontext is included on request headers`() {
        val testServerInterceptor = TestServerInterceptor()

        // Create a fake in process server
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, testServerInterceptor)).build()
                .start()
        )

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Register KonigKontextClientInterceptor
        val blockingStub =
            GreeterGrpc.newBlockingStub(channel).withKonigKontextInterceptor(TestCustomKontextKey)

        // Build KonigKontext which has type HelloRequest
        val konigKontextValue = "my_custom_value_1234"

        GrpcContext.current().withValue(TestCustomKontextKey.grpcContextKey, konigKontextValue).run {
            blockingStub.sayHello(HelloRequest.getDefaultInstance())
        }

        assertNotNull(testServerInterceptor.capturedMetadata)
        assertNotNull(testServerInterceptor.capturedMetadata!!.get(TestCustomKontextKey.grpcHeaderKey))
        assertEquals(konigKontextValue, String(testServerInterceptor.capturedMetadata!!.get(TestCustomKontextKey.grpcHeaderKey)!!))
        assertEquals(konigKontextValue, TestCustomKontextKey.valueFromBinary(testServerInterceptor.capturedMetadata!!.get(TestCustomKontextKey.grpcHeaderKey)!!))
    }

    @Test
    fun `Given downstream rpc, when calling rpc from KonigKontext intercepted client and the protobuf KonigKontext is set to the default value, then the default KonigKontext is included on request headers`() {
        val testServerInterceptor = TestServerInterceptor()

        // Create a fake in process server
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, testServerInterceptor)).build()
                .start()
        )

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Register KonigKontextClientInterceptor
        val blockingStub =
            GreeterGrpc.newBlockingStub(channel).withKonigKontextInterceptor(TestProtobufKontextKey)

        GrpcContext.current().withValue(TestProtobufKontextKey.grpcContextKey, TestProtobufKontextKey.defaultValue).run {
            blockingStub.sayHello(HelloRequest.getDefaultInstance())
        }

        assertNotNull(testServerInterceptor.capturedMetadata)
        assertNotNull(testServerInterceptor.capturedMetadata!!.get(TestProtobufKontextKey.grpcHeaderKey))
        assertEquals(HelloRequest.getDefaultInstance(), HelloRequest.parseFrom(testServerInterceptor.capturedMetadata!!.get(TestProtobufKontextKey.grpcHeaderKey)))
        assertEquals(HelloRequest.getDefaultInstance(), TestProtobufKontextKey.valueFromBinary(testServerInterceptor.capturedMetadata!!.get(TestProtobufKontextKey.grpcHeaderKey)!!))
    }

    @Test
    fun `Given downstream rpc, when calling rpc from KonigKontext intercepted client and the custom KonigKontext is set to the default value, then the default KonigKontext is included on request headers`() {
        val testServerInterceptor = TestServerInterceptor()

        // Create a fake in process server
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(ServerInterceptors.intercept(object : GreeterImplBase() {
                    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
                        val response = HelloReply.getDefaultInstance()
                        responseObserver?.onNext(response)
                        responseObserver?.onCompleted()
                    }
                }, testServerInterceptor)).build()
                .start()
        )

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        // Register KonigKontextClientInterceptor
        val blockingStub =
            GreeterGrpc.newBlockingStub(channel).withKonigKontextInterceptor(TestCustomKontextKey)

        GrpcContext.current().withValue(TestCustomKontextKey.grpcContextKey, TestCustomKontextKey.defaultValue).run {
            blockingStub.sayHello(HelloRequest.getDefaultInstance())
        }

        assertNotNull(testServerInterceptor.capturedMetadata)
        assertNotNull(testServerInterceptor.capturedMetadata!!.get(TestCustomKontextKey.grpcHeaderKey))
        assertEquals("", String(testServerInterceptor.capturedMetadata!!.get(TestCustomKontextKey.grpcHeaderKey)!!))
        assertEquals("", TestCustomKontextKey.valueFromBinary(testServerInterceptor.capturedMetadata!!.get(TestCustomKontextKey.grpcHeaderKey)!!))
    }
}
