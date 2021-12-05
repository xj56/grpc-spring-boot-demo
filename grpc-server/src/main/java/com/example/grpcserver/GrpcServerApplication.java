package com.example.grpcserver;

import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.IOException;

@SpringBootApplication
@Slf4j
public class GrpcServerApplication {

    @Autowired
    private GrpcServiceDemo serviceDemo;

    public static void main(String[] args) {
        SpringApplication.run(GrpcServerApplication.class, args);
    }

    @PostConstruct
    public void server() {
        try {
            ServerBuilder.forPort(6666).addService(serviceDemo).build().start();
            log.info("server start at 6666....");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
