# gRPC Java Demo - 四种通信模式

本项目演示 gRPC 的四种通信模式，使用 Java 和 Maven 构建。

## 项目结构

```
java-demo/
├── grpc-basic/                    # 一元 RPC (Unary RPC)
├── grpc-server-stream/            # 服务端流式 RPC (Server Streaming RPC)
├── grpc-client-stream/            # 客户端流式 RPC (Client Streaming RPC)
├── grpc-bidirectional-stream/     # 双向流式 RPC (Bidirectional Streaming RPC)
└── pom.xml                        # 父 POM 文件
```

## 环境要求

- Java 11 或更高版本
- Maven 3.6 或更高版本

## 构建项目

**重要**: 不需要手动运行 protoc 编译 proto 文件！Maven 会自动处理。

在项目根目录执行：

```bash
mvn clean install
```

这个命令会自动：
1. 调用 protoc 编译所有 `.proto` 文件
2. 生成 Java 代码（消息类、服务类、Stub 类等）
3. 将生成的代码放在 `target/generated-sources/protobuf/` 目录
4. 编译整个项目

### 生成的代码结构

编译后，每个模块的 `target/generated-sources/protobuf/` 目录会包含：
```
target/generated-sources/protobuf/
├── java/                          # 消息类
│   └── com/example/grpc/.../
│       ├── HelloRequest.java
│       └── HelloResponse.java
└── grpc-java/                     # gRPC 服务类
    └── com/example/grpc/.../
        └── HelloServiceGrpc.java  # 包含 Stub 和服务基类
```

**注意**: `target/` 目录中的代码是自动生成的，不应该手动修改。每次 `mvn clean` 后会被删除，需要重新编译。

## 四种 gRPC 通信模式

### 1. 一元 RPC (Unary RPC) - grpc-basic

**概念**: 客户端发送一个请求，服务端返回一个响应。这是最简单的模式，类似于普通的函数调用。

**示例场景**: 简单的问候服务

**Proto 定义**:
```protobuf
rpc SayHello (HelloRequest) returns (HelloResponse);
```

**运行**:
```bash
# 启动服务端 (端口 50051)
cd grpc-basic
mvn exec:java -Dexec.mainClass="com.example.grpc.basic.server.HelloServer"

# 启动客户端 (在另一个终端)
mvn exec:java -Dexec.mainClass="com.example.grpc.basic.client.HelloClient"
```

**特点**:
- 一对一通信
- 最常用的模式
- 适合简单的请求-响应场景

---

### 2. 服务端流式 RPC (Server Streaming RPC) - grpc-server-stream

**概念**: 客户端发送一个请求，服务端返回多个响应（流）。

**示例场景**: 请求一个范围内的所有数字，服务端逐个返回

**Proto 定义**:
```protobuf
rpc GetNumbers (NumberRequest) returns (stream NumberResponse);
```

**运行**:
```bash
# 启动服务端 (端口 50052)
cd grpc-server-stream
mvn exec:java -Dexec.mainClass="com.example.grpc.serverstream.server.NumberServer"

# 启动客户端 (在另一个终端)
mvn exec:java -Dexec.mainClass="com.example.grpc.serverstream.client.NumberClient"
```

**特点**:
- 一个请求，多个响应
- 服务端可以持续发送数据
- 适合返回列表、实时数据推送等场景

---

### 3. 客户端流式 RPC (Client Streaming RPC) - grpc-client-stream

**概念**: 客户端发送多个请求（流），服务端返回一个响应。

**示例场景**: 客户端发送一组数字，服务端计算并返回平均值

**Proto 定义**:
```protobuf
rpc CalculateAverage (stream NumberMessage) returns (AverageResponse);
```

**运行**:
```bash
# 启动服务端 (端口 50053)
cd grpc-client-stream
mvn exec:java -Dexec.mainClass="com.example.grpc.clientstream.server.AverageServer"

# 启动客户端 (在另一个终端)
mvn exec:java -Dexec.mainClass="com.example.grpc.clientstream.client.AverageClient"
```

**特点**:
- 多个请求，一个响应
- 客户端可以持续发送数据
- 适合批量上传、数据聚合等场景

---

### 4. 双向流式 RPC (Bidirectional Streaming RPC) - grpc-bidirectional-stream

**概念**: 客户端和服务端都可以发送多个消息（流），双方可以同时读写。

**示例场景**: 聊天功能，客户端和服务端实时交互

**Proto 定义**:
```protobuf
rpc Chat (stream ChatRequest) returns (stream ChatResponse);
```

**运行**:
```bash
# 启动服务端 (端口 50054)
cd grpc-bidirectional-stream
mvn exec:java -Dexec.mainClass="com.example.grpc.bistream.server.ChatServer"

# 启动客户端 (在另一个终端)
mvn exec:java -Dexec.mainClass="com.example.grpc.bistream.client.ChatClient"
```

**特点**:
- 多个请求，多个响应
- 双方可以同时发送和接收
- 适合聊天、实时协作等场景

---

## 模式对比总结

| 模式 | 客户端发送 | 服务端返回 | 使用场景 |
|------|-----------|-----------|---------|
| 一元 RPC | 1个请求 | 1个响应 | 简单查询、API调用 |
| 服务端流 | 1个请求 | 多个响应 | 数据推送、列表查询 |
| 客户端流 | 多个请求 | 1个响应 | 批量上传、数据聚合 |
| 双向流 | 多个请求 | 多个响应 | 聊天、实时协作 |

## 关键概念

### StreamObserver

在 gRPC Java 中，`StreamObserver` 是处理流式数据的核心接口：

- `onNext(T value)`: 发送/接收一个数据项
- `onError(Throwable t)`: 处理错误
- `onCompleted()`: 标记流完成

### Stub 类型

- **BlockingStub**: 阻塞式调用，用于一元 RPC 和服务端流
- **AsyncStub**: 异步调用，用于客户端流和双向流

## 技术栈

- gRPC: 1.58.0
- Protocol Buffers: 3.24.0
- Java: 11+
- Maven: 3.6+

## 学习建议

1. 从 `grpc-basic` 开始，理解基本的 gRPC 工作原理
2. 学习 `grpc-server-stream`，理解服务端如何发送流
3. 学习 `grpc-client-stream`，理解客户端如何发送流
4. 最后学习 `grpc-bidirectional-stream`，理解双向通信

每个模块都包含详细的代码注释，建议阅读源码以深入理解。
