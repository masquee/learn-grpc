# gRPC Go Demo - 四种通信模式

本项目演示 gRPC 的四种通信模式，使用 Go 语言和 Protocol Buffers 构建。

## 项目结构

```
go-demo/
├── grpc-basic/                    # 一元 RPC (Unary RPC)
│   ├── proto/hello.proto
│   ├── server/main.go
│   └── client/main.go
├── grpc-server-stream/            # 服务端流式 RPC (Server Streaming RPC)
│   ├── proto/number.proto
│   ├── server/main.go
│   └── client/main.go
├── grpc-client-stream/            # 客户端流式 RPC (Client Streaming RPC)
│   ├── proto/average.proto
│   ├── server/main.go
│   └── client/main.go
├── grpc-bidirectional-stream/     # 双向流式 RPC (Bidirectional Streaming RPC)
│   ├── proto/chat.proto
│   ├── server/main.go
│   └── client/main.go
├── go.mod
└── README.md
```

## 环境要求

- Go 1.19 或更高版本
- Protocol Buffers 编译器 (protoc)
- gRPC Go 插件

## 安装依赖

### 1. 安装 Protocol Buffers 编译器

**macOS:**
```bash
brew install protobuf
```

**Linux:**
```bash
apt install -y protobuf-compiler
# 或
yum install -y protobuf-compiler
```

**验证安装:**
```bash
protoc --version  # 应该显示 libprotoc 3.x.x 或更高版本
```

### 2. 安装 gRPC Go 插件

```bash
go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
```

确保 `$GOPATH/bin` 在你的 `PATH` 环境变量中：
```bash
export PATH="$PATH:$(go env GOPATH)/bin"
```

## 构建项目

**重要**: 需要先编译 proto 文件生成 Go 代码！

在项目根目录执行：

```bash
# 1. 安装 Go 依赖
go mod tidy

# 2. 编译所有 proto 文件
cd grpc-basic && protoc --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative proto/hello.proto && cd ..
cd grpc-server-stream && protoc --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative proto/number.proto && cd ..
cd grpc-client-stream && protoc --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative proto/average.proto && cd ..
cd grpc-bidirectional-stream && protoc --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative proto/chat.proto && cd ..
```

或者使用更简单的方式（推荐）：

```bash
# 使用 find 命令一次性编译所有 proto 文件
find . -name "*.proto" -exec protoc --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative {} \;

# 然后安装依赖
go mod tidy
```

### 生成的代码结构

编译后，每个模块的 `proto/` 目录会生成两个文件：
```
proto/
├── hello.proto              # 原始定义文件
├── hello.pb.go              # Protocol Buffers 消息类型代码
└── hello_grpc.pb.go         # gRPC 服务接口代码
```

**注意**: `*.pb.go` 和 `*_grpc.pb.go` 文件是自动生成的，不应该手动修改。如果修改了 `.proto` 文件，需要重新运行 `protoc` 命令。

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
cd grpc-basic/server
go run main.go

# 启动客户端 (在另一个终端)
cd grpc-basic/client
go run main.go
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
cd grpc-server-stream/server
go run main.go

# 启动客户端 (在另一个终端)
cd grpc-server-stream/client
go run main.go
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
cd grpc-client-stream/server
go run main.go

# 启动客户端 (在另一个终端)
cd grpc-client-stream/client
go run main.go
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
cd grpc-bidirectional-stream/server
go run main.go

# 启动客户端 (在另一个终端)
cd grpc-bidirectional-stream/client
go run main.go
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

### Stream 接口

在 gRPC Go 中，流式处理通过以下接口实现：

**服务端流**:
```go
// 服务端使用 stream.Send() 发送多个响应
func (s *server) GetNumbers(req *pb.NumberRequest, stream pb.NumberService_GetNumbersServer) error {
    for i := start; i <= end; i++ {
        stream.Send(&pb.NumberResponse{Number: i})
    }
    return nil
}

// 客户端使用 stream.Recv() 接收多个响应
stream, _ := client.GetNumbers(ctx, req)
for {
    resp, err := stream.Recv()
    if err == io.EOF {
        break
    }
    // 处理响应
}
```

**客户端流**:
```go
// 客户端使用 stream.Send() 发送多个请求
stream, _ := client.CalculateAverage(ctx)
for _, num := range numbers {
    stream.Send(&pb.NumberMessage{Number: num})
}
resp, _ := stream.CloseAndRecv()

// 服务端使用 stream.Recv() 接收多个请求
func (s *server) CalculateAverage(stream pb.AverageService_CalculateAverageServer) error {
    for {
        req, err := stream.Recv()
        if err == io.EOF {
            return stream.SendAndClose(&pb.AverageResponse{Average: avg})
        }
        // 处理请求
    }
}
```

**双向流**:
```go
// 双方都可以同时使用 Send() 和 Recv()
stream, _ := client.Chat(ctx)

// 发送协程
go func() {
    for _, msg := range messages {
        stream.Send(&pb.ChatRequest{Message: msg})
    }
    stream.CloseSend()
}()

// 接收协程
for {
    resp, err := stream.Recv()
    if err == io.EOF {
        break
    }
    // 处理响应
}
```

### Context 使用

Go 的 gRPC 实现大量使用 `context.Context` 来控制超时和取消：

```go
// 设置超时
ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
defer cancel()

// 使用 context
resp, err := client.SayHello(ctx, req)
```

## 技术栈

- gRPC: google.golang.org/grpc
- Protocol Buffers: google.golang.org/protobuf
- Go: 1.19+

## 学习建议

1. 从 `grpc-basic` 开始，理解基本的 gRPC 工作原理
2. 学习 `grpc-server-stream`，理解服务端如何发送流
3. 学习 `grpc-client-stream`，理解客户端如何发送流
4. 最后学习 `grpc-bidirectional-stream`，理解双向通信

每个模块都包含详细的代码注释，建议阅读源码以深入理解。

## 常见问题

### 1. 编译 proto 文件时出错

确保已经安装了 `protoc` 和相关插件：
```bash
protoc --version
which protoc-gen-go
which protoc-gen-go-grpc
```

### 2. 导入路径错误

检查 `go.mod` 中的 module 名称是否与代码中的 import 路径一致。

### 3. 端口被占用

如果端口被占用，可以修改各个 server/main.go 中的端口号。

## 与 Java 版本的对比

- **代码生成**: Java 使用 Maven 插件自动生成，Go 需要手动运行 `protoc` 命令
- **并发模型**: Go 使用 goroutine 和 channel，Java 使用 StreamObserver 回调
- **错误处理**: Go 使用 error 返回值，Java 使用 onError 回调
- **上下文管理**: Go 使用 context.Context，Java 没有内置的上下文概念（但可以使用 Deadline）

## 参考资料

- [gRPC Go 官方文档](https://grpc.io/docs/languages/go/)
- [Protocol Buffers Go 教程](https://protobuf.dev/getting-started/gotutorial/)
- [gRPC Go Examples](https://github.com/grpc/grpc-go/tree/master/examples)
