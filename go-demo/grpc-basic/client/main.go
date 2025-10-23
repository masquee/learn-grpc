package main

import (
	"context"
	"log"
	"time"

	pb "github.com/example/grpc-go-demo/grpc-basic/proto"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

func main() {
	// 连接服务端
	conn, err := grpc.Dial("localhost:50051", grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatalf("Failed to connect: %v", err)
	}
	defer conn.Close()

	client := pb.NewHelloServiceClient(conn)

	// 创建上下文
	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()

	// 发送请求
	req := &pb.HelloRequest{Name: "gRPC"}
	log.Printf("Sending request: %v", req.GetName())

	// 一元 RPC：发送一个请求，接收一个响应
	resp, err := client.SayHello(ctx, req)
	if err != nil {
		log.Fatalf("Failed to call SayHello: %v", err)
	}

	log.Printf("Received: %v", resp.GetMessage())
}
