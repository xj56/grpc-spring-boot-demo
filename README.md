# grpc-spring-boot-demo
grpc springboot demo
Grpc简单教程

`api`包定义`grpc`服务，`server`和`client`引用`api`包

![image-20211205175251031](C:\Users\xj\AppData\Roaming\Typora\typora-user-images\image-20211205175251031.png)

# 引入依赖

定义`api`包，添加如下依赖

```xml
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
    <version>1.42.1</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
    <version>1.42.1</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
    <version>1.42.1</version>
</dependency>
```

配置插件(参考官方文档)

```xml
    <build>
        <extensions>
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>1.6.2</version>
            </extension>
        </extensions>
        <plugins>
            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <version>0.6.1</version>
                <extensions>true</extensions>
                <configuration>
                    <protocArtifact>com.google.protobuf:protoc:3.17.2:exe:${os.detected.classifier}
                    </protocArtifact>
                    <pluginId>grpc-java</pluginId>
                    <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.42.1:exe:${os.detected.classifier}
                    </pluginArtifact>
                    <!--默认值-->
                    <protoSourceRoot>src/main/proto</protoSourceRoot>
                    <!--默认值-->
                    <!--<outputDirectory>${project.build.directory}/generated-sources/protobuf/java</outputDirectory>-->
                    <outputDirectory>src/main/java</outputDirectory>
                    <!--设置是否在生成java文件之前清空outputDirectory的文件，默认值为true，设置为false时也会覆盖同名文件-->
                    <clearOutputDirectory>false</clearOutputDirectory>
                    <!--更多配置信息可以查看https://www.xolstice.org/protobuf-maven-plugin/compile-mojo.html-->
                </configuration>
                <executions>
                    <execution>
                        <!--在执行mvn compile的时候会执行以下操作-->
                        <phase>compile</phase>
                        <goals>
                            <!--生成OuterClass类-->
                            <goal>compile</goal>
                            <!--生成Grpc类-->
                            <goal>compile-custom</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

# 定义服务

main目录下新建`proto`包用来存放`proto`文件，

![image-20211205170359509](C:\Users\xj\AppData\Roaming\Typora\typora-user-images\image-20211205170359509.png)

例如以下：

```protobuf
syntax = "proto3";

//Java文件生成路径
option java_package = "com.example.grpc.api";

//默认等于proto文件名
option java_outer_classname = "HelloGrpcProto";

//定义服务请求的实体传参
message User {
    string name = 1;
}

//定义服务响应实体
message HelloMessage {
    string msg = 1;
}

service Hello {
    rpc SayHello(User) returns (HelloMessage);
}
```

# 生成服务

然后执行`mvn install`命令，生成`rpc`文件：

![image-20211205170503678](C:\Users\xj\AppData\Roaming\Typora\typora-user-images\image-20211205170503678.png)

接下来说一下这2个文件：

`HelloGrpc`类包含自定义服务生成的`stub`和`implbase`等：

![image-20211205170821615](C:\Users\xj\AppData\Roaming\Typora\typora-user-images\image-20211205170821615.png)

`HelloGrpcProto`包含定义的message生成的类：

![image-20211205171436452](C:\Users\xj\AppData\Roaming\Typora\typora-user-images\image-20211205171436452.png)

# 服务端使用

作为服务端，引入前面定义的`api`包

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>grpc-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

服务的具体逻辑需要继承`HelloImplBase` ，该抽象类包含自定义的服务里的方法，实现该方法的逻辑即可：

```java
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
```

`HelloMessage`使用`newBuilder`进行构建，然后使用`onNext`方法发送消息，`onCompleted`通知流完成消息发送

启动服务，绑定端口和服务，这里通过`Autowired`注入定义好的服务，然后调用`addService`方法绑定服务，

```java
	@Autowired
    private GrpcServiceDemo serviceDemo;

    @PostConstruct
    public void server() {
        try {
            ServerBuilder.forPort(6666).addService(serviceDemo).build().start();
            log.info("server start at 6666....");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```

# 客户端使用

客户端同样引入`api`包，然后构建`channel`，并绑定到`stub`，通过`stub`调用服务定义`sayHello`的方法向服务器发送消息，然后打印服务器返回的消息：

```java
public void client() {
    final ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 6666).usePlaintext().build();
    final HelloGrpc.HelloBlockingStub stub = HelloGrpc.newBlockingStub(channel);
    final HelloGrpcProto.User user = HelloGrpcProto.User.newBuilder().setName("xxx").build();
    final HelloGrpcProto.HelloMessage message = stub.sayHello(user);
    log.info("response from server: {}", message);
}
```
