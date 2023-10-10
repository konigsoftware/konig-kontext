Konig Kontext - A globally shared context for JVM based gRPC microservice architectures
====================================================================================

Do you have a gRPC microservices architecture? Have you ever needed a value deep in your call stack but the value is
only known several RPC's ago in the request lifetime? Hate having to update multiple RPC requests/responses in order to
pass this value down the stack, leading to new code branches and more unit tests? Konig Kontext was developed for this
exact frustration.

See https://github.com/konigsoftware/konig-kontext/tree/main/examples/example-kotlin for a Kotlin example project that implements this
library. See https://github.com/konigsoftware/konig-kontext/tree/main/examples/example-java for an equivalent example in Java.

Installation
------------

### Gradle:

<details open>
<summary>Kotlin</summary>
<br>
Add the following to your `build.gradle.kts`:

```kotlin
implementation("com.konigsoftware:konig-kontext:1.0.0")
```
</details>

<details>
<summary>Groovy</summary>
<br>
Add the following to your `build.gradle`:

```groovy
implementation 'com.konigsoftware:konig-kontext:1.0.0'
```
</details>


### Maven:

Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>com.konigsoftware</groupId>
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

### 1. Create KonigKontextKey

First define an object that extends `KonigKontextKey` in a shared location and implement the interface. **All of your microservices should have access to this key**.
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

It can be a good practice to use a protobuf message as the type of the value associated with your `KonigKontextKey`. Using a protobuf message
allows you to have a global type definition for your context value, while also making it easy to make updates to the type without shooting yourself
in the foot. 

This library contains special helpers to encourage using a protobuf message. As explained above, we still need to define an object 
that extends `KonigKontextKey` in a global location, but we can use the helper class `KonigKontextProtobufKey` to make this a bit easier.
The following examples define a key with a value of type `MyContextMessage`, where `MyContextMessage` extends `com.google.protobuf.Message`:

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

Again, all of your microservices should have access to the key defined above.

### 2. Client side setup:

The client side setup just has one step. Simply add the `KonigKontextClientInterceptor` to all of your gRPC clients and
pass in your previously defined `KonigKontextKey`:

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

The server side setup also just has one step. Simply add the `KonigKontextServerInterceptor` to all of your gRPC servers
and again pass in your previously defined `KonigKontextKey`:

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

That's it! No more setup required. See the Usage section below for next steps on actually using the library.

Usage
-----

### Setting value for KonigKontextKey:

<details open>
<summary>Kotlin</summary>

```kotlin
withKonigKontext(KonigKontext.withValue(MyContextKey, /* ADD VALUE HERE */)) {
    // Remaining code path that will have access to the updated KonigKontext
}
```
</details>

<details>
<summary>Java</summary>

```java
KonigKontext.withValue(GlobalContextKeys.MY_CONTEXT_KEY, /* set value here */).run(() -> {
    // Remaining code path that will have access to the updated KonigKontext 
})
```
</details>

### Getting value for KonigKontextKey:

The code within the closure provided to `withKonigKontext` and `run` (see above) as well as _any_ downstream RPC's called from within that
closure will have access to the value keyed by your `KonigKontextKey`. To access the value from any downstream RPC simply call:

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

### Use Cases:

The true value add of this library is the ability to access a KonigKontext value N number of RPC's after it has been is set.
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
request messages seems a bit unnecessary. However, with KonigKontext, you can simply set the user id for a `KonigKontextKey` 
in the public facing service immediately after authentication, and then all the downstream services from A to E will
automagically have access to the value on that key.

See https://github.com/konigsoftware/konig-kontext/tree/main/examples/example-kotlin and https://github.com/konigsoftware/konig-kontext/tree/main/examples/example-java
for example projects in both Kotlin and Java that utilize KonigKontext for a situation similar to the one described here.