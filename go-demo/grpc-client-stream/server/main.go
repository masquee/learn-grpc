package main

import (
	"fmt"
	"io"
	"log"
	"net"

	pb "github.com/example/grpc-go-demo/grpc-client-stream/proto"
	"google.golang.org/grpc"
)

// server 实现 AverageService
type server struct {
	pb.UnimplementedAverageServiceServer
}

// CalculateAverage 实现客户端流式 RPC
func (s *server) CalculateAverage(stream pb.AverageService_CalculateAverageServer) error {
	log.Println("Client started streaming numbers")

	var sum int32
	var count int32

	// 客户端流：接收多个请求
	for {
		req, err := stream.Recv()
		if err == io.EOF {
			// 客户端完成发送，计算平均值并返回
			var average float64
			if count > 0 {
				average = float64(sum) / float64(count)
			}
			log.Printf("Calculated average: %.2f (sum=%d, count=%d)", average, sum, count)
			return stream.SendAndClose(&pb.AverageResponse{Average: average})
		}
		if err != nil {
			return err
		}

		number := req.GetNumber()
		log.Printf("Received: %d", number)
		sum += number
		count++
	}
}

func main() {
	port := 50053
	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", port))
	if err != nil {
		log.Fatalf("Failed to listen: %v", err)
	}

	s := grpc.NewServer()
	pb.RegisterAverageServiceServer(s, &server{})

	log.Printf("Server started, listening on port %d", port)
	if err := s.Serve(lis); err != nil {
		log.Fatalf("Failed to serve: %v", err)
	}
}
