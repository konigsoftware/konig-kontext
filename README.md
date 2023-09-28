Konig Kontext - A globally shared context for JVM based gRPC microservice architectures
====================================================================================

Do you have a gRPC microservices architecture? Have you ever needed a value deep in your call stack but the value is
only known several RPC's ago in the request lifetime? Hate having to update multiple RPC requests/responses in order to
pass this value down the stack, leading to new code branches and more unit tests? Konig Kontext was developed for this
exact frustration.

See https://github.com/konigsoftware/konig-kontext-example-kotlin for a Kotlin example project that implements this
library. See https://github.com/konigsoftware/konig-kontext-example-java for an equivalent example in Java.

Installation
------------

### Gradle:

Add the following to your `build.gradle.kts`:

```kotlin
implementation("org.konigsoftware:konig-kontext:1.0.0")
```

or for Groovy add the following to your `build.gradle`:

```groovy
implementation 'org.konigsoftware:konig-kontext:1.0.0'
```

### Maven:

Add the following to your `pom.xml`:

```xml

<dependency>
    <groupId>org.konigsoftware</groupId>
    <artifactId>konig-kontext</artifactId>
    <version>1.0.0</version>
</dependency>
```

Setup
-----

This library allows you to define and pass around any arbitrary context between all of your microservices. The context
can be of any type you like, as long as you can provide parsers to convert the type to and from a binary format.
However, it is a recommended practice to use a protobuf `Message` to define the context type. Examples for using both a
protobuf `Message` based KonigKontext and a custom KonigKontext will be explained below:

### 1. Initialize KonigKontext

#### Protobuf Message Based KonigKontext:

If you are using a protobuf `Message` for your context type, first you need to define the protobuf message that will be
used:

```protobuf
syntax = "proto3";

package example.shared;

message MyContext {
  // Add any fields you like here   
}
```

Next declare an instance of `KonigKontext` in a shared location. All of your microservices should have access to this
value:

Kotlin:

```kotlin
val MY_GLOBAL_KONTEXT: KonigKontext<MyContext> = KonigKontext.fromProtoMessage(MyContext::class)
```

Java:

```java
public class GlobalKontext {
    public static final KonigKontext<MyContext> MY_GLOBAL_KONTEXT = KonigKontext.fromProtoMessageJava(MyContext.class);
}
```

#### Custom KonigKontext type:

If you are using a protobuf `Message` based KonigKontext, you can ignore the following setup. Next declare a custom
instance of `KonigKontext` in a shared location passing in a default value for your type, and function to conver the
type to and from a byte array. All of your microservices should have access to this value:

Kotlin:

```kotlin
val MY_GLOBAL_KONTEXT: KonigKontext<MyCustomType> = KonigKontext(
    /* Default MyCustomType value here */,
    { /* Convert MyCustomType to a ByteArray here */ },
    { /* Convert a ByteArray to MyCustomTypeHere */ }
)
```

Java:

```java
public class GlobalKontext {
    public static final KonigKontext<MyCustomType> MY_GLOBAL_KONTEXT = KonigKontext(
            /* Default MyCustomType value here */,
            { /* Convert MyCustomType to a ByteArray here */},
            { /* Convert a ByteArray to MyCustomTypeHere */}
    );
}
```

### 2. Client side setup:

The client side setup just has one step. Simply add the `KonigKontextClientInterceptor` to all of your gRPC clients and
pass in your previously defined KonigKontext instance:

Kotlin:

```kotlin
val myServiceClient = MyServiceCoroutineStub(myManagedChannel)
    .withInterceptors(KonigKontextClientInterceptor(MY_GLOBAL_KONTEXT))

// Or for a slightly more succinct way you can optionally use the Kotlin idiomatic helper function instead:
val myServiceClient = MyServiceCoroutineStub(myManagedChannel)
    .withKonigKontextInterceptor(MY_GLOBAL_KONTEXT)
```

Java:

```java
MyServiceBlockingStub myServiceClient=MyServiceGrpc
        .newBlockingStub(ManagedChannelBuilder.forTarget("port here").usePlaintext().build())
        .withInterceptors(new KonigKontextClientInterceptor<>(GlobalKontext.MY_GLOBAL_KONTEXT));
```

### 3. Server side setup:

The server side setup also just has one step. Simply add the `KonigKontextServerInterceptor` to all of your gRPC servers
and pass in your previously defined KonigKontext instance:

Kotlin:

```kotlin
val myServer = ServerBuilder
    .forPort(<port here>)
    .addService(MyService())
    .intercept(KonigKontextServerInterceptor(MY_GLOBAL_KONTEXT))
    .build()

// Or for a slightly more succinct way you can optionally use the Kotlin idiomatic helper function instead:
val myServer = ServerBuilder
    .forPort(<port here>)
    .addService(MyService())
    .withKonigKontextInterceptor(MY_GLOBAL_KONTEXT)
    .build() 
```

Java:

```java
Server myServer = ServerBuilder
        .forPort(<port here>)
        .addService(new MyService())
        .intercept(new KonigKontextServerInterceptor<>(GlobalKontext.MY_GLOBAL_KONTEXT))
        .build();
```

That's it! No more setup required. See the Usage section below for next steps on actually using the library.

Usage
-----

### Setting KonigKontext

Setting the KonigKontext differs depending on the language you are using. See the following snippets for your language:

Kotlin:
```kotlin
withKonigKontext(MY_GLOBAL_KONTEXT.setValue(/* set value here */)) {
    // Remaining code path that will have access to the updated KonigKontext
}
```

Java:
```java
GlobalKontext.MY_GLOBAL_KONTEXT.setValue(/* set value here */).run(() -> {
    // Remaining code path that will have access to the updated KonigKontext 
})
```

### Getting Konig Kontext

The code within the closure provided to `withKonigKontext` and `run` (see above) as well as _any_ downstream RPC's called from within that
closure will have access to the new KonigKontext. To access the context from any downstream RPC simply call:

Kotlin:
```kotlin
MY_GLOBAL_KONTEXT.get()
```

Java:
```java
GlobalKontext.MY_GLOBAL_KONTEXT.get()
```

### Use Cases:

The true value add of this library is the ability to access the KonigKontext N number of RPC's after it has been is set.
To explain this lets look at an example use case. Assume you have a typical api gateway microservices architecture with
one public facing service and several internal microservices that are not publicly accessible. The public facing service
performs authentication on incoming requests before routing the requests to downstream microservices for further
processing.

Since the public facing service performs authentication on incoming requests, it likely has access to the user id making
the request or some other authentication related data. After authenticating, assume a downstream RPC is called on a
different microservice, lets call it service A. Now what if service A calls another RPC on service B which calls another
RPC on service C and so on until maybe we get to service E where we now need access to the user id.

Without KonigKontext you would have to add the user id to all the RPC request messages in between the public service and
service E. All these changes might require new unit tests and thorough code reviews, using up development time.
Additionally, the user id might not be needed on the RPC's between service A and service D, and so including it on the
request messages seems a bit unnecessary. However, with KonigKontext, you can simply add the user id to the KonigKontext
from the public facing service immediately after authentication and all the downstream services from A to E will
automagically have access to the user id simply by calling `MY_GLOBAL_KONTEXT.get()`.

See https://github.com/konigsoftware/konig-kontext-example-kotlin and https://github.com/konigsoftware/konig-kontext-example-java
for example projects in both Kotlin and Java that utilize KonigKontext for a situation similar to the one described here.