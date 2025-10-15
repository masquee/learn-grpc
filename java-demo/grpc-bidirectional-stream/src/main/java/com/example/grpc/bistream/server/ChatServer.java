package com.example.grpc.bistream.server;

import com.example.grpc.bistream.ChatRequest;
import com.example.grpc.bistream.ChatResponse;
import com.example.grpc.bistream.ChatServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ChatServer {
    private Server server;

    private void start() throws IOException {
        int port = 50054;
        server = ServerBuilder.forPort(port)
                .addService(new ChatServiceImpl())
                .build()
                .start();
        System.out.println("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down gRPC server");
            try {
                ChatServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final ChatServer server = new ChatServer();
        server.start();
        server.blockUntilShutdown();
    }

    // 实现服务
    static class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {
        @Override
        public StreamObserver<ChatRequest> chat(StreamObserver<ChatResponse> responseObserver) {
            // 返回一个 StreamObserver<ChatRequest> 来接收客户端流
            // 同时可以通过 responseObserver (StreamObserver<ChatResponse>) 发送服务端流
            return new StreamObserver<ChatRequest>() {
                @Override
                public void onNext(ChatRequest request) {
                    // 接收客户端消息
                    System.out.println("Received from " + request.getUser() + ": " + request.getMessage());

                    // 立即回复消息（双向流：可以同时发送和接收）
                    ChatResponse response = ChatResponse.newBuilder()
                            .setUser("Server")
                            .setMessage("Echo: " + request.getMessage())
                            .build();

                    responseObserver.onNext(response);
                    System.out.println("Sent to client: Echo: " + request.getMessage());
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    // 客户端完成发送
                    System.out.println("Client has completed sending messages");

                    // 发送最后一条消息
                    ChatResponse farewell = ChatResponse.newBuilder()
                            .setUser("Server")
                            .setMessage("Goodbye!")
                            .build();
                    responseObserver.onNext(farewell);

                    // 完成响应
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
