# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a gRPC Java demonstration project showcasing the four core gRPC communication patterns. It's organized as a Maven multi-module project with each module demonstrating a different RPC pattern.

## Build Commands

**Important**: No need to manually run `protoc` to compile proto files. Maven handles this automatically via `protobuf-maven-plugin`.

```bash
# Build all modules and generate protobuf code automatically
mvn clean install

# Build a specific module
cd <module-name>
mvn clean compile

# Clean generated protobuf code
mvn clean
```

The build process automatically:
1. Invokes protoc to compile `.proto` files
2. Generates Java classes (messages, services, stubs)
3. Places generated code in `target/generated-sources/protobuf/java/` and `grpc-java/`
4. Compiles the entire project

## Running Services

Each module runs on a different port and requires two terminals (server + client):

**grpc-basic (port 50051)**
```bash
# Terminal 1
cd grpc-basic && mvn exec:java -Dexec.mainClass="com.example.grpc.basic.server.HelloServer"
# Terminal 2
cd grpc-basic && mvn exec:java -Dexec.mainClass="com.example.grpc.basic.client.HelloClient"
```

**grpc-server-stream (port 50052)**
```bash
cd grpc-server-stream && mvn exec:java -Dexec.mainClass="com.example.grpc.serverstream.server.NumberServer"
cd grpc-server-stream && mvn exec:java -Dexec.mainClass="com.example.grpc.serverstream.client.NumberClient"
```

**grpc-client-stream (port 50053)**
```bash
cd grpc-client-stream && mvn exec:java -Dexec.mainClass="com.example.grpc.clientstream.server.AverageServer"
cd grpc-client-stream && mvn exec:java -Dexec.mainClass="com.example.grpc.clientstream.client.AverageClient"
```

**grpc-bidirectional-stream (port 50054)**
```bash
cd grpc-bidirectional-stream && mvn exec:java -Dexec.mainClass="com.example.grpc.bistream.server.ChatServer"
cd grpc-bidirectional-stream && mvn exec:java -Dexec.mainClass="com.example.grpc.bistream.client.ChatClient"
```

## Architecture

### Module Structure
Each module follows the same pattern:
- `src/main/proto/` - Protocol Buffer definitions
- `src/main/java/com/example/grpc/<module>/server/` - Server implementation
- `src/main/java/com/example/grpc/<module>/client/` - Client implementation
- Generated code appears in `target/generated-sources/` after compilation

### Code Generation Flow
1. `.proto` files define service interfaces and message types
2. `protobuf-maven-plugin` automatically generates Java classes during Maven compile phase (no manual protoc needed)
3. Generated classes include:
   - Message classes (e.g., `HelloRequest`, `HelloResponse`)
   - Service base classes (e.g., `HelloServiceGrpc.HelloServiceImplBase`)
   - Stub classes for clients (e.g., `HelloServiceGrpc.HelloServiceBlockingStub`)
4. Generated code location: `target/generated-sources/protobuf/{java,grpc-java}/`
5. Generated code is temporary and not committed to git - regenerate after `mvn clean` or fresh clone

### gRPC Pattern Implementations

**Unary RPC (grpc-basic)**
- Server extends `*ServiceImplBase` and implements service method with `StreamObserver<Response>` parameter
- Client uses `BlockingStub` for synchronous calls
- Pattern: `rpc Method(Request) returns (Response)`

**Server Streaming (grpc-server-stream)**
- Server calls `responseObserver.onNext()` multiple times before `onCompleted()`
- Client uses `BlockingStub` and receives `Iterator<Response>`
- Pattern: `rpc Method(Request) returns (stream Response)`

**Client Streaming (grpc-client-stream)**
- Server method returns `StreamObserver<Request>` to receive client stream
- Client uses `AsyncStub` and gets `StreamObserver<Request>` to send multiple messages
- Pattern: `rpc Method(stream Request) returns (Response)`

**Bidirectional Streaming (grpc-bidirectional-stream)**
- Server method returns `StreamObserver<Request>` and receives `StreamObserver<Response>`
- Client uses `AsyncStub` with request and response observers
- Both sides can read/write independently
- Pattern: `rpc Method(stream Request) returns (stream Response)`

### Key Concepts

**StreamObserver Interface**
- `onNext(T value)` - Send/receive a message
- `onError(Throwable t)` - Handle errors
- `onCompleted()` - Signal stream completion

**Stub Types**
- `BlockingStub` - Synchronous, blocking calls (unary and server streaming)
- `AsyncStub` - Asynchronous calls with callbacks (client and bidirectional streaming)

## Dependencies

- gRPC: 1.58.0
- Protobuf: 3.24.0
- Java: 11+
- Maven: 3.6+

## Modifying Proto Files

After editing `.proto` files, regenerate code (no manual protoc invocation needed):
```bash
mvn clean compile
```

**Key Points**:
- Maven automatically invokes protoc during compilation
- Generated classes are in `target/generated-sources/` (not source-controlled)
- Must regenerate after: `mvn clean`, fresh clone, or proto changes
- Do not manually edit generated code - edit `.proto` files instead

## Port Assignments

- 50051: grpc-basic (Unary RPC)
- 50052: grpc-server-stream (Server Streaming)
- 50053: grpc-client-stream (Client Streaming)
- 50054: grpc-bidirectional-stream (Bidirectional Streaming)
