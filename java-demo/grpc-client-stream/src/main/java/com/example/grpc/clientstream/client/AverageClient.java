package com.example.grpc.clientstream.client;

import com.example.grpc.clientstream.AverageResponse;
import com.example.grpc.clientstream.AverageServiceGrpc;
import com.example.grpc.clientstream.NumberMessage;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AverageClient {
    private final ManagedChannel channel;
    private final AverageServiceGrpc.AverageServiceStub asyncStub;

    public AverageClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        // 使用异步 stub，因为客户端流需要异步操作
        asyncStub = AverageServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void calculateAverage(int[] numbers) throws InterruptedException {
        System.out.println("Sending numbers to calculate average...");

        CountDownLatch finishLatch = new CountDownLatch(1);

        // 创建响应观察者来接收服务端的响应
        StreamObserver<AverageResponse> responseObserver = new StreamObserver<AverageResponse>() {
            @Override
            public void onNext(AverageResponse response) {
                System.out.println("Received average: " + response.getAverage());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed sending response");
                finishLatch.countDown();
            }
        };

        // 获取请求观察者来发送流式请求（客户端流：多个请求，一个响应）
        StreamObserver<NumberMessage> requestObserver = asyncStub.calculateAverage(responseObserver);

        try {
            // 发送多个数字
            for (int number : numbers) {
                NumberMessage request = NumberMessage.newBuilder()
                        .setNumber(number)
                        .build();
                requestObserver.onNext(request);
                System.out.println("Sent: " + number);

                // 模拟延迟
                Thread.sleep(200);
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        // 完成发送
        requestObserver.onCompleted();

        // 等待响应
        finishLatch.await(1, TimeUnit.MINUTES);
    }

    public static void main(String[] args) throws Exception {
        AverageClient client = new AverageClient("localhost", 50053);
        try {
            // 发送一组数字计算平均值
            int[] numbers = {10, 20, 30, 40, 50};
            client.calculateAverage(numbers);
        } finally {
            client.shutdown();
        }
    }
}
