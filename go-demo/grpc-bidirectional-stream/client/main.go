package main

import (
	"context"
	"io"
	"log"
	"time"

	pb "github.com/example/grpc-go-demo/grpc-bidirectional-stream/proto"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

func main() {
	// 连接服务端
	conn, err := grpc.Dial("localhost:50054", grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatalf("Failed to connect: %v", err)
	}
	defer conn.Close()

	client := pb.NewChatServiceClient(conn)

	// 创建上下文
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	// 双向流：可以同时发送和接收
	stream, err := client.Chat(ctx)
	if err != nil {
		log.Fatalf("Failed to call Chat: %v", err)
	}

	// 启动 goroutine 接收消息
	waitc := make(chan struct{})
	go func() {
		for {
			resp, err := stream.Recv()
			if err == io.EOF {
				// 服务端完成发送
				log.Println("Server has completed sending messages")
				close(waitc)
				return
			}
			if err != nil {
				log.Fatalf("Failed to receive: %v", err)
			}
			log.Printf("Received from %s: %s", resp.GetUser(), resp.GetMessage())
		}
	}()

	// 发送一系列聊天消息
	messages := []string{
		"Hello!",
		"How are you?",
		"I'm learning gRPC!",
		"Bidirectional streaming is cool!",
	}

	log.Println("Starting chat...")
	for _, msg := range messages {
		req := &pb.ChatRequest{
			User:    "Client",
			Message: msg,
		}
		if err := stream.Send(req); err != nil {
			log.Fatalf("Failed to send: %v", err)
		}
		log.Printf("Sent: %s", msg)
		time.Sleep(1 * time.Second) // 模拟延迟
	}

	// 完成发送
	if err := stream.CloseSend(); err != nil {
		log.Fatalf("Failed to close send: %v", err)
	}

	// 等待接收完成
	<-waitc
}
