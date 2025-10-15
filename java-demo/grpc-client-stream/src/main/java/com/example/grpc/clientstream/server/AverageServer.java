package com.example.grpc.clientstream.server;

import com.example.grpc.clientstream.AverageResponse;
import com.example.grpc.clientstream.AverageServiceGrpc;
import com.example.grpc.clientstream.NumberMessage;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AverageServer {
    private Server server;

    private void start() throws IOException {
        int port = 50053;
        server = ServerBuilder.forPort(port)
                .addService(new AverageServiceImpl())
                .build()
                .start();
        System.out.println("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down gRPC server");
            try {
                AverageServer.this.stop();
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
        final AverageServer server = new AverageServer();
        server.start();
        server.blockUntilShutdown();
    }

    // 实现服务
    static class AverageServiceImpl extends AverageServiceGrpc.AverageServiceImplBase {
        @Override
        public StreamObserver<NumberMessage> calculateAverage(StreamObserver<AverageResponse> responseObserver) {
            // 返回一个 StreamObserver 来接收客户端流
            return new StreamObserver<NumberMessage>() {
                int sum = 0;
                int count = 0;

                @Override
                public void onNext(NumberMessage request) {
                    // 接收客户端发送的每个数字
                    int number = request.getNumber();
                    sum += number;
                    count++;
                    System.out.println("Received: " + number + " (sum=" + sum + ", count=" + count + ")");
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    // 客户端完成发送后，计算平均值并返回
                    double average = count > 0 ? (double) sum / count : 0;
                    System.out.println("Calculating average: " + sum + " / " + count + " = " + average);

                    AverageResponse response = AverageResponse.newBuilder()
                            .setAverage(average)
                            .build();

                    // 发送响应
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
