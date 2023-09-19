Konig Kontext - A globally shared context for Kotlin gRPC microservice architectures
====================================================================================

Do you have a gRPC microservices architecture? Have you ever needed a value deep in your call stack but the value
is only known several RPC's ago in the request lifetime? Hate having to update multiple RPC requests/responses in order to pass this value down
the stack, leading to new code branches and more unit tests? Konig Kontext was developed for this exact frustration. 

See https://github.com/konigsoftware/konig-kontext-example for an example project that implements this library.

Installation
------------

### Gradle:
Add the following to your build.gradle.kts:
```gradle
implementation("org.konigsoftware:konig-kontext:1.0.0")
```
or for Groovy add the following to your build.gradle:
```gradle
implementation 'org.konigsoftware:konig-kontext:1.0.0'
```

Setup
-----

This library allows you to define and pass around an arbitrary context, in the format of a protobuf message, between all 
your gRPC microservices. First you need to define the protobuf message format that will be used for the shared context:
```protobuf
syntax = "proto3";

package example.shared;

message MyContext {
  // Add any fields you like here   
}
```
Next there are a few boilerplate items that you need to take care of on both the client and server side.

### Client side setup:

The client side setup just has one step. Simply add the `KonigKontextClientInterceptor` to all of your gRPC clients:

```kotlin
val myServiceClient = MyServiceCoroutineStub(myManagedChannel).withInterceptors(
    KonigKontextClientInterceptor()
)

// Or for a slightly more succinct way you can optionally use the idiomatic helper function instead:
val myServiceClient = MyServiceCoroutineStub(myManagedChannel).withKonigKontextInterceptor()
```

### Server side setup:

On the server side, there are two steps required. First extend the `KonigKontextServer` interface in all of your gRPC service
implementations and pass in the context protobuf message you created above as the type parameter:
```kotlin
class MyService : MyServiceCoroutineImplBase(), KonigKontextServer<MyContext> {
    // Your service implementation here
}
```
Then when building and running your services, add the `KonigKontextServerInterceptor` with the `addKonigKontextServer` builder function:
```kotlin
val myServiceServer = ServerBuilder
    .forPort(<your_port_here>)
    .addKonigKontextServer(MyService(), MyContext::class)
    .build()
```

That's it! No more setup required. See the Usage section below for next steps on actually using the library.

Usage
-----

### Setting Konig Kontext

To update the Konig Kontext, call `withKonigKontext` supplying your updated context and a closure containing the remaining
code in the request path:
```kotlin
val myNewContext = myContext { 
    // Set your fields here
}

withKonigKontext(myNewContext) {
    // Remaining code path that will have access to the KonigKontext
}
```

### Getting Konig Kontext

The code within the closure provided to `withKonigKontext` as well as _any_ downstream RPC's called from within that closure 
will have access to the new KonigKontext. To access the context from any downstream RPC simply call `konigKontext()`:
```kotlin
class MyDownstreamService : MyDownstreamServiceCoroutineImplBase(), KonigKontextServer<MyContext> {
    override suspend fun myDownstreamRpc(request: Request): Response {
        val fetchedContext = konigKontext()
        
        // other processing here
        
        return Response
    }
} 
```
The true value add of this library is the ability to access the KonigKontext N number of RPC's after the Kontext is set.
To explain this lets look at an example use case. Assume you have a typical api gateway microservices architecture with
one public facing service and several internal microservices that are not publicly accessible. 
The public facing service performs authentication on incoming requests before routing the requests to downstream 
microservices for further processing.

Since the public facing service performs authentication on incoming requests, it likely has access to the user id making the request or some other
authentication related data. After authenticating, assume a downstream RPC is called on a different microservice, lets call it service A. 
Now what if service A calls another RPC on service B which calls another RPC on service C and so on until maybe we get to service E
where we now need access to the user id. 

Without KonigKontext you would have to add the user id to all the RPC request messages
in between the public service and service E. All these changes might require new unit tests and thorough code reviews, using up
development time. Additionally, the user id might not be needed on the RPC's between service A and service D, and so including it on the request
messages seems a bit unnecessary. However, with KonigKontext, you can simply add the user id to the KonigKontext from the 
public facing service immediately after authentication and all the downstream services from A to E will automagically have access
to the user id simply by calling `konigKontext()`.

See https://github.com/konigsoftware/konig-kontext-example for an example project that utilizes KonigKontext for a situation
similar to the one described here.