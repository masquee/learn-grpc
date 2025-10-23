package main

import (
	"fmt"
	"io"
	"log"
	"net"

	pb "github.com/example/grpc-go-demo/grpc-bidirectional-stream/proto"
	"google.golang.org/grpc"
)

// server 实现 ChatService
type server struct {
	pb.UnimplementedChatServiceServer
}

// Chat 实现双向流式 RPC
func (s *server) Chat(stream pb.ChatService_ChatServer) error {
	log.Println("Chat started")

	// 双向流：可以同时接收和发送
	for {
		// 接收客户端消息
		req, err := stream.Recv()
		if err == io.EOF {
			// 客户端完成发送
			log.Println("Client has completed sending messages")

			// 发送最后一条消息
			farewell := &pb.ChatResponse{
				User:    "Server",
				Message: "Goodbye!",
			}
			if err := stream.Send(farewell); err != nil {
				return err
			}
			log.Printf("Sent to client: %s", farewell.Message)
			return nil
		}
		if err != nil {
			return err
		}

		log.Printf("Received from %s: %s", req.GetUser(), req.GetMessage())

		// 立即回复消息（双向流：可以同时发送和接收）
		resp := &pb.ChatResponse{
			User:    "Server",
			Message: fmt.Sprintf("Echo: %s", req.GetMessage()),
		}
		if err := stream.Send(resp); err != nil {
			return err
		}
		log.Printf("Sent to client: %s", resp.Message)
	}
}

func main() {
	port := 50054
	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", port))
	if err != nil {
		log.Fatalf("Failed to listen: %v", err)
	}

	s := grpc.NewServer()
	pb.RegisterChatServiceServer(s, &server{})

	log.Printf("Server started, listening on port %d", port)
	if err := s.Serve(lis); err != nil {
		log.Fatalf("Failed to serve: %v", err)
	}
}
