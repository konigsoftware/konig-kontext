Konig Kontext - A globally shared, request scoped, context for JVM based gRPC microservices
====================================================================================

[![Gradle Build Status](https://github.com/konigsoftware/konig-kontext/actions/workflows/build.yaml/badge.svg?query=branch=main)](https://github.com/konigsoftware/konig-kontext/actions/workflows/build.yaml?query=branch%3Amain)

[![konig-kontext](https://img.shields.io/maven-central/v/com.konigsoftware/konig-kontext.svg?label=konig-kontext)](https://central.sonatype.com/search?q=com.konigsoftware%3Akonig-kontext&smo=true)

A request context propagation framework which can carry values across gRPC microservice boundaries. Example context values might include:
- Security principals, or user credentials and identifiers. Add a user credential to the KonigKontext early in a request lifetime, and later access the credential from a different microservice.
- Distributed tracing information. Add a request trace id to the KonigKontext upon receiving a request and later access that id in any downstream microservice. 

Konig Kontext is built to support any type of context value, so it can be extended to fit _your_ specific use cases as well. 

Installation
------------

### Gradle:

<details open>
<summary>Kotlin</summary>
<br>
Add the following to your `build.gradle.kts`:

```kotlin
implementation("com.konigsoftware:konig-kontext:1.1.0")
```
</details>

<details>
<summary>Groovy</summary>
<br>
Add the following to your `build.gradle`:

```groovy
implementation 'com.konigsoftware:konig-kontext:1.1.0'
```
</details>


### Maven:

Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>com.konigsoftware</groupId>
    <artifactId>konig-kontext</artifactId>
    <version>1.1.0</version>
</dependency>
```

Setup
-----

### 1. Create KonigKontextKey

KonigKontext values are indexed by a `KonigKontextKey`. To define a key, create an object that extends `KonigKontextKey` and implement the interface. This object should live in a shared package
accessible to all of your microservices.

The following examples define a key with a value of type `String` in both Kotlin and Java, although you can use _any_ type you like instead:

<details open>
<summary>Kotlin</summary>

```kotlin
object MyContextKey : KonigKontextKey<String>() {
    override val defaultValue: String = ""

    override fun valueFromBinary(binaryValue: ByteArray): String = String(binaryValue)

    override fun valueToBinary(value: String): ByteArray = value.toByteArray()
}
```
</details>

<details>
<summary>Java</summary>

```java
public class GlobalContextKeys {
    public static final KonigKontextKey<String> MY_CONTEXT_KEY = new KonigKontextKey<>() {
        @Override
        public String getDefaultValue() {
            return "";
        }

        @Override
        public byte[] valueToBinary(String s) {
            return s.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String valueFromBinary(byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    };
}
```
</details>


#### Protobuf Message Based Type:

Using a protobuf message to type your Konig Kontext value is a good practice, as it allows for type changes in a backwards-compatible way. 
There's a helper class, `KonigKontextProtobufKey`, that makes this easier. The following examples define a key with a 
value of type `MyContextMessage`, where `MyContextMessage` extends `com.google.protobuf.Message`:

<details open>
<summary>Kotlin</summary>

```kotlin
object MyContextKey : KonigKontextProtobufKey<MyContextMessage>(MyContextMessage::class)
```
</details>

<details>
<summary>Java</summary>

```java
public class GlobalContextKeys {
    public static final KonigKontextProtobufKey<MyContextMessage> MY_CONTEXT_KEY = KonigKontextProtobufKey.fromJavaClass(MyContextMessage.class);
};
```
</details>

### 2. Client side setup:

Include the `KonigKontextClientInterceptor` in all gRPC clients where you want to propagate the current `KonigKontext`. 
Provide your previously defined `KonigKontextKey` to the constructor:

<details open>
<summary>Kotlin</summary>

```kotlin
val myServiceClient = MyServiceCoroutineStub(myManagedChannel)
    .withInterceptors(KonigKontextClientInterceptor(MyContextKey))

// Or for a slightly more succinct way you can optionally use the Kotlin idiomatic helper function instead:
val myServiceClient = MyServiceCoroutineStub(myManagedChannel)
    .withKonigKontextInterceptor(MyContextKey)
```
</details>

<details>
<summary>Java</summary>

```java
MyServiceBlockingStub myServiceClient = MyServiceGrpc
        .newBlockingStub(ManagedChannelBuilder.forTarget("port here").usePlaintext().build())
        .withInterceptors(new KonigKontextClientInterceptor<>(GlobalContextKeys.MY_CONTEXT_KEY));
```
</details>

### 3. Server side setup:

Include the `KonigKontextServerInterceptor` in all gRPC servers that require access to the `KonigKontext`. Provide your 
previously defined `KonigKontextKey` to the constructor.

<details open>
<summary>Kotlin</summary>

```kotlin
val myServer = ServerBuilder
    .forPort(<port here>)
    .addService(MyService())
    .intercept(KonigKontextServerInterceptor(MyContextKey))
    .build()

// Or for a slightly more succinct way you can optionally use the Kotlin idiomatic helper function instead:
val myServer = ServerBuilder
    .forPort(<port here>)
    .addService(MyService())
    .withKonigKontextInterceptor(MyContextKey)
    .build() 
```
</details>

<details>
<summary>Java</summary>

```java
Server myServer = ServerBuilder
        .forPort(<port here>)
        .addService(new MyService())
        .intercept(new KonigKontextServerInterceptor<>(GlobalContextKeys.MY_CONTEXT_KEY))
        .build();
```
</details>

Usage
-----

To set and get `KonigKontext` values, follow these steps:

### Setting value:

<details open>
<summary>Kotlin</summary>

```kotlin
withKonigKontext(KonigKontext.withValue(MyContextKey, /* ADD VALUE HERE */)) {
    // Remaining code path that will have access to the updated KonigKontext

    // Any client stub RPC called from within this lambda will automatically give that RPC
    // access to the current KonigKontext. 
}
```
</details>

<details>
<summary>Java</summary>

```java
KonigKontext.withValue(GlobalContextKeys.MY_CONTEXT_KEY, /* set value here */).run(() -> {
    // Remaining code path that will have access to the updated KonigKontext
    
    // Any client stub RPC called from within this lambda will automatically give that RPC
    // access to the current KonigKontext.    
})
```
</details>

### Getting value:

Any KonigKontext scoped closure (see `withKonigKontext` and `run` above) will have access to get a KonigKontext value. Any
downstream microservice RPC called from within a KonigKontext scoped closure can also access a previously set KonigKontext value.

Access a KonigKontext value:

<details open>
<summary>Kotlin</summary>

```kotlin
KonigKontext.getValue(MyContextKey)
```

</details>

<details>
<summary>Java</summary>

```java
KonigKontext.getValue(GlobalContextKeys.MY_CONTEXT_KEY)
```
</details>

### Usage Example:

Let's consider two services, ServiceA and ServiceB, running in separate containers. ServiceA handles an incoming request and then calls ServiceB:

<details open>
<summary>Kotlin</summary>

```kotlin
// Service A:
class ServiceA {
    val serviceBClient = ServiceBCoroutineStub(myManagedChannel)
        .withInterceptors(KonigKontextClientInterceptor(MyContextKey)) 
    
    suspend fun handleRequest(userId: String): Response {
        return withKonigKontext(KonigKontext.withValue(MyKontextKey, userId)) {
            println("USER ID: ${KonigKontext.getValue(MyContextKey)}")
            
            val serviceBResponse = serviceBClient.doSomething(doSomethingRequest { })
            
            Response(serviceBResponse)
        }
    }
}
```
```kotlin
// Service B:
class ServiceB : ServiceBCoroutineImplBase() {
    override suspend fun doSomething(DoSomethingRequest: request): Response {
        val userId = KonigKontext.getValue(MyContextKey)
        
        println("USER ID: $userId")
        
        return Response()
    }
}
```
</details>

<details>
<summary>Java</summary>

```java
// Service A:
public class ServiceA {
    var serviceBClient = ServiceBGrpc.newBlockingStub(myManagedChannel)
        .withInterceptors(new KonigKontextClientInterceptor<>(GlobalContextKeys.MY_CONTEXT_KEY)) 
    
    public Response handleRequest(String userId) {
        var responseBuilder = Response.builder();
        
        KonigKontext.withValue(GlobalContextKeys.MY_CONTEXT_KEY, userId).run(() -> {
            System.out.println("USER ID: " + userId);
            
            var getBalanceResponse = serviceBClient.doSomething(DoSomethingRequest.newBuilder().build());
            
            responseBuilder.setResponse(getBalanceResponse);
        });
        
        return responseBuilder.build(); 
    }
}
```
```java
// Service B:
public class ServiceB extends ServiceBImplBase {
    @Override
    public void doSomething(DoSomethingRequest request, StreamObserver<DoSomethingResponse> responseObserver) {
        var userId = KonigKontext.getValue(MyContextKey);
        
        println("USER ID: " + userId);

        responseObserver.onNext(DoSomethinResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
```
</details>

Calling `ServiceA.handleRequest("some_user_id")` would first print: `USER ID: some_user_id` in ServiceA and then also in ServiceB.
Since `serviceBClient.doSomething()` is called from within a KonigKontext scoped closure, the current KonigKontext is automatically propagated
to ServiceB, even though the two services are running in entirely separate containers.

### Full Examples:

See full example implementations in:
- [Kotlin](https://github.com/konigsoftware/konig-kontext/tree/main/examples/example-kotlin)
- [Java](https://github.com/konigsoftware/konig-kontext/tree/main/examples/example-java) 