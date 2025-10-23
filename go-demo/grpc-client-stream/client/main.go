package main

import (
	"context"
	"log"
	"time"

	pb "github.com/example/grpc-go-demo/grpc-client-stream/proto"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

func main() {
	// 连接服务端
	conn, err := grpc.Dial("localhost:50053", grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatalf("Failed to connect: %v", err)
	}
	defer conn.Close()

	client := pb.NewAverageServiceClient(conn)

	// 创建上下文
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	// 客户端流：发送多个请求
	stream, err := client.CalculateAverage(ctx)
	if err != nil {
		log.Fatalf("Failed to call CalculateAverage: %v", err)
	}

	// 发送一组数字
	numbers := []int32{10, 20, 30, 40, 50}
	log.Println("Sending numbers...")
	for _, num := range numbers {
		req := &pb.NumberMessage{Number: num}
		if err := stream.Send(req); err != nil {
			log.Fatalf("Failed to send: %v", err)
		}
		log.Printf("Sent: %d", num)
		time.Sleep(500 * time.Millisecond) // 模拟延迟
	}

	// 完成发送并接收响应
	resp, err := stream.CloseAndRecv()
	if err != nil {
		log.Fatalf("Failed to receive response: %v", err)
	}

	log.Printf("Average: %.2f", resp.GetAverage())
}
