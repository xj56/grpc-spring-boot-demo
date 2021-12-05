package com.example.grpcserver;


import com.example.grpc.api.HelloGrpc;
import com.example.grpc.api.HelloGrpcProto;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GrpcServiceDemo extends HelloGrpc.HelloImplBase {

    @Override
    public void sayHello(HelloGrpcProto.User request, StreamObserver<HelloGrpcProto.HelloMessage> responseObserver) {
        final HelloGrpcProto.HelloMessage message = HelloGrpcProto.HelloMessage
                .newBuilder().setMsg("hello " + request.getName()).build();
        log.info("receive from client: {}", request);
        responseObserver.onNext(message);
        responseObserver.onCompleted();
    }
}
