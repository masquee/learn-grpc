package main

import (
	"context"
	"io"
	"log"
	"time"

	pb "github.com/example/grpc-go-demo/grpc-server-stream/proto"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

func main() {
	// 连接服务端
	conn, err := grpc.Dial("localhost:50052", grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatalf("Failed to connect: %v", err)
	}
	defer conn.Close()

	client := pb.NewNumberServiceClient(conn)

	// 创建上下文
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	// 发送请求
	req := &pb.NumberRequest{Start: 1, End: 5}
	log.Printf("Requesting numbers from %d to %d", req.GetStart(), req.GetEnd())

	// 服务端流：接收多个响应
	stream, err := client.GetNumbers(ctx, req)
	if err != nil {
		log.Fatalf("Failed to call GetNumbers: %v", err)
	}

	// 接收流中的所有数字
	for {
		resp, err := stream.Recv()
		if err == io.EOF {
			// 流结束
			log.Println("Stream completed")
			break
		}
		if err != nil {
			log.Fatalf("Failed to receive: %v", err)
		}
		log.Printf("Received: %d", resp.GetNumber())
	}
}
