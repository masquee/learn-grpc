package main

import (
	"fmt"
	"log"
	"net"
	"time"

	pb "github.com/example/grpc-go-demo/grpc-server-stream/proto"
	"google.golang.org/grpc"
)

// server 实现 NumberService
type server struct {
	pb.UnimplementedNumberServiceServer
}

// GetNumbers 实现服务端流式 RPC
func (s *server) GetNumbers(req *pb.NumberRequest, stream pb.NumberService_GetNumbersServer) error {
	start := req.GetStart()
	end := req.GetEnd()

	log.Printf("Received request: start=%d, end=%d", start, end)

	// 服务端流：逐个发送数字
	for i := start; i <= end; i++ {
		resp := &pb.NumberResponse{Number: i}
		if err := stream.Send(resp); err != nil {
			return err
		}
		log.Printf("Sent: %d", i)
		time.Sleep(500 * time.Millisecond) // 模拟延迟
	}

	return nil
}

func main() {
	port := 50052
	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", port))
	if err != nil {
		log.Fatalf("Failed to listen: %v", err)
	}

	s := grpc.NewServer()
	pb.RegisterNumberServiceServer(s, &server{})

	log.Printf("Server started, listening on port %d", port)
	if err := s.Serve(lis); err != nil {
		log.Fatalf("Failed to serve: %v", err)
	}
}
