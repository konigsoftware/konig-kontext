package konig.kontext

import io.grpc.ClientInterceptors
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
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.konigsoftware.konig.kontext.KonigKontextClientInterceptor

class KonigKontextClientInterceptorTest {
    @Rule
    val grpcCleanup = GrpcCleanupRule()

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
    fun `Given downstream rpc, when calling rpc from KonigKontext intercepted client and the KonigKontext is empty, then the KonigKontext is not included on request headers`() {
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
        val blockingStub =
            GreeterGrpc.newBlockingStub(ClientInterceptors.intercept(channel, KonigKontextClientInterceptor()))

        blockingStub.sayHello(HelloRequest.getDefaultInstance())

        assertNotNull(testServerInterceptor.capturedMetadata)
        assertNull(testServerInterceptor.capturedMetadata!!.get(KONIG_KONTEXT_GRPC_HEADER_KEY))
    }

    @Test
    fun `Given downstream rpc, when calling rpc from KonigKontext intercepted client and the KonigKontext is set by withGrpcContext, then the KonigKontext is included on request headers`() {
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
        val blockingStub =
            GreeterGrpc.newBlockingStub(ClientInterceptors.intercept(channel, KonigKontextClientInterceptor()))

        // Build KonigKontext which has type HelloRequest
        val konigKontext = HelloRequest.newBuilder().setName("my_custom_field_1234").build()

        withGrpcContext(GrpcContext.current().withValue(KONIG_KONTEXT_GRPC_CONTEXT_KEY, konigKontext)) {
            blockingStub.sayHello(HelloRequest.getDefaultInstance())
        }

        assertNotNull(testServerInterceptor.capturedMetadata)
        assertNotNull(testServerInterceptor.capturedMetadata!!.get(KONIG_KONTEXT_GRPC_HEADER_KEY))
        assertEquals(konigKontext, HelloRequest.parseFrom(testServerInterceptor.capturedMetadata!!.get(KONIG_KONTEXT_GRPC_HEADER_KEY)))
    }

    @Test
    fun `Given downstream rpc, when calling rpc from KonigKontext intercepted client and the KonigKontext is set by withKonigKontext, then the KonigKontext is included on request headers`() = runBlocking {
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
        val blockingStub =
            GreeterGrpc.newBlockingStub(ClientInterceptors.intercept(channel, KonigKontextClientInterceptor()))

        // Build KonigKontext which has type HelloRequest
        val konigKontext = HelloRequest.newBuilder().setName("my_custom_field_1234").build()

        withKonigKontext(konigKontext) {
            blockingStub.sayHello(HelloRequest.getDefaultInstance())
        }

        assertNotNull(testServerInterceptor.capturedMetadata)
        assertNotNull(testServerInterceptor.capturedMetadata!!.get(KONIG_KONTEXT_GRPC_HEADER_KEY))
        assertEquals(konigKontext, HelloRequest.parseFrom(testServerInterceptor.capturedMetadata!!.get(KONIG_KONTEXT_GRPC_HEADER_KEY)))
    }
}