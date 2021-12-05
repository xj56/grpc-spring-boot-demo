package com.example.client;

import com.example.grpc.api.HelloGrpc;
import com.example.grpc.api.HelloGrpcProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
@Slf4j
public class GrpcDemoClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrpcDemoClientApplication.class, args);
    }

    @PostConstruct
    public void client() {
        final ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 6666).usePlaintext().build();
        final HelloGrpc.HelloBlockingStub stub = HelloGrpc.newBlockingStub(channel);
        final HelloGrpcProto.User user = HelloGrpcProto.User.newBuilder().setName("xxx").build();
        final HelloGrpcProto.HelloMessage message = stub.sayHello(user);
        log.info("response from server: {}", message);
    }
}
