package main

import (
	"context"
	"fmt"
	"log"
	"net"

	pb "github.com/example/grpc-go-demo/grpc-basic/proto"
	"google.golang.org/grpc"
)

// server 实现 HelloService
type server struct {
	pb.UnimplementedHelloServiceServer
}

// SayHello 实现一元 RPC
func (s *server) SayHello(ctx context.Context, req *pb.HelloRequest) (*pb.HelloResponse, error) {
	log.Printf("Received: %v", req.GetName())
	message := fmt.Sprintf("Hello, %s!", req.GetName())
	return &pb.HelloResponse{Message: message}, nil
}

func main() {
	port := 50051
	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", port))
	if err != nil {
		log.Fatalf("Failed to listen: %v", err)
	}

	s := grpc.NewServer()
	pb.RegisterHelloServiceServer(s, &server{})

	log.Printf("Server started, listening on port %d", port)
	if err := s.Serve(lis); err != nil {
		log.Fatalf("Failed to serve: %v", err)
	}
}
