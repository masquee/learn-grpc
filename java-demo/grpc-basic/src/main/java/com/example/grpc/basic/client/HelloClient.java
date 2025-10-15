package com.example.grpc.basic.client;

import com.example.grpc.basic.HelloRequest;
import com.example.grpc.basic.HelloResponse;
import com.example.grpc.basic.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public class HelloClient {
    private final ManagedChannel channel;
    private final HelloServiceGrpc.HelloServiceBlockingStub blockingStub;

    public HelloClient(String host, int port) {
        // 创建通道
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        // 创建阻塞式 stub
        blockingStub = HelloServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void greet(String name) {
        System.out.println("Sending request: " + name);

        // 构建请求
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();

        // 发送请求并接收响应（一元 RPC：一个请求，一个响应）
        HelloResponse response = blockingStub.sayHello(request);

        System.out.println("Received response: " + response.getMessage());
    }

    public static void main(String[] args) throws Exception {
        HelloClient client = new HelloClient("localhost", 50051);
        try {
            // 发送多个请求测试
            client.greet("Alice");
            client.greet("Bob");
            client.greet("World");
        } finally {
            client.shutdown();
        }
    }
}
