package com.example.grpc.bistream.client;

import com.example.grpc.bistream.ChatRequest;
import com.example.grpc.bistream.ChatResponse;
import com.example.grpc.bistream.ChatServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ChatClient {
    private final ManagedChannel channel;
    private final ChatServiceGrpc.ChatServiceStub asyncStub;

    public ChatClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        asyncStub = ChatServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void chat(String[] messages) throws InterruptedException {
        System.out.println("Starting chat...");

        CountDownLatch finishLatch = new CountDownLatch(1);

        // 创建响应观察者来接收服务端的流式消息（StreamObserver<ChatResponse>）
        StreamObserver<ChatResponse> responseObserver = new StreamObserver<ChatResponse>() {
            @Override
            public void onNext(ChatResponse response) {
                System.out.println("Received from " + response.getUser() + ": " + response.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed sending messages");
                finishLatch.countDown();
            }
        };

        // 获取请求观察者来发送流式消息（StreamObserver<ChatRequest>）
        // 双向流：可以同时发送和接收
        StreamObserver<ChatRequest> requestObserver = asyncStub.chat(responseObserver);

        try {
            // 发送多条消息
            for (String message : messages) {
                ChatRequest chatRequest = ChatRequest.newBuilder()
                        .setUser("Client")
                        .setMessage(message)
                        .build();
                requestObserver.onNext(chatRequest);
                System.out.println("Sent: " + message);

                // 模拟延迟，让服务端有时间处理和响应
                Thread.sleep(1000);
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        // 完成发送
        requestObserver.onCompleted();

        // 等待服务端完成响应
        finishLatch.await(1, TimeUnit.MINUTES);
    }

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient("localhost", 50054);
        try {
            // 发送一系列聊天消息
            String[] messages = {
                "Hello!",
                "How are you?",
                "I'm learning gRPC!",
                "Bidirectional streaming is cool!"
            };
            client.chat(messages);
        } finally {
            client.shutdown();
        }
    }
}
