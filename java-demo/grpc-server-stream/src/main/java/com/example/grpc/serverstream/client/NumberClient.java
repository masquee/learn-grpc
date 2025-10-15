package com.example.grpc.serverstream.client;

import com.example.grpc.serverstream.NumberRequest;
import com.example.grpc.serverstream.NumberResponse;
import com.example.grpc.serverstream.NumberServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class NumberClient {
    private final ManagedChannel channel;
    private final NumberServiceGrpc.NumberServiceBlockingStub blockingStub;

    public NumberClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        blockingStub = NumberServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void getNumbers(int start, int end) {
        System.out.println("Requesting numbers from " + start + " to " + end);

        // 构建请求
        NumberRequest request = NumberRequest.newBuilder()
                .setStart(start)
                .setEnd(end)
                .build();

        // 发送请求并接收流式响应（服务端流：一个请求，多个响应）
        Iterator<NumberResponse> responses = blockingStub.getNumbers(request);

        // 遍历所有响应
        while (responses.hasNext()) {
            NumberResponse response = responses.next();
            System.out.println("Received: " + response.getNumber());
        }

        System.out.println("All numbers received");
    }

    public static void main(String[] args) throws Exception {
        NumberClient client = new NumberClient("localhost", 50052);
        try {
            // 请求数字 1 到 5
            client.getNumbers(1, 5);
        } finally {
            client.shutdown();
        }
    }
}
