package com.example.grpc.serverstream.server;

import com.example.grpc.serverstream.NumberRequest;
import com.example.grpc.serverstream.NumberResponse;
import com.example.grpc.serverstream.NumberServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class NumberServer {
    private Server server;

    private void start() throws IOException {
        int port = 50052;
        server = ServerBuilder.forPort(port)
                .addService(new NumberServiceImpl())
                .build()
                .start();
        System.out.println("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down gRPC server");
            try {
                NumberServer.this.stop();
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
        final NumberServer server = new NumberServer();
        server.start();
        server.blockUntilShutdown();
    }

    // 实现服务
    static class NumberServiceImpl extends NumberServiceGrpc.NumberServiceImplBase {
        @Override
        public void getNumbers(NumberRequest request, StreamObserver<NumberResponse> responseObserver) {
            int start = request.getStart();
            int end = request.getEnd();

            System.out.println("Received request: start=" + start + ", end=" + end);

            // 服务端流式 RPC：发送多个响应
            for (int i = start; i <= end; i++) {
                NumberResponse response = NumberResponse.newBuilder()
                        .setNumber(i)
                        .build();

                // 发送每个数字
                responseObserver.onNext(response);
                System.out.println("Sent: " + i);

                // 模拟延迟
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // 完成流
            responseObserver.onCompleted();
            System.out.println("Stream completed");
        }
    }
}
