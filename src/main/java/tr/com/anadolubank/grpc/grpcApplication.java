package tr.com.anadolubank.grpc;

import org.apache.camel.component.grpc.auth.jwt.JwtCallCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ResourceLoader;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import tr.com.anadolubank.grpc.proto.HelloRequest;
import tr.com.anadolubank.grpc.proto.HelloResponse;
import tr.com.anadolubank.grpc.proto.HelloServiceGrpc;

@SpringBootApplication
@ImportResource({"classpath:spring/camel-context.xml"})
public class grpcApplication  implements CommandLineRunner {
	
	final String JWT_SECRET_KEY="TUNgUL5Bs8kjVvWnPYwPJhaM";
	
	@Autowired
	ResourceLoader resourceLoader;

	public static void main(String[] args) {
		SpringApplication.run(grpcApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET_KEY);
		String token = JWT.create().sign(algorithm);
		CallCredentials callCreds = new JwtCallCredentials(token);
		
		
		NettyChannelBuilder builder = NettyChannelBuilder.forAddress("localhost", 1101);
		SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient()
												.sslProvider(SslProvider.OPENSSL)
												.keyManager(resourceLoader.getResource("classpath:certs/client_certificate.pem").getInputStream(), 
															resourceLoader.getResource("classpath:certs/client_private_key.key").getInputStream())
												/*.trustManager(InsecureTrustManagerFactory.INSTANCE)*/
												.trustManager(resourceLoader.getResource("classpath:certs/server_certificate.pem").getInputStream());
		
		io.netty.handler.ssl.SslContext sslContext = sslContextBuilder.build();
		
		
		builder = builder.sslContext(sslContext);
		
		ManagedChannel channel = builder.negotiationType(NegotiationType.TLS)
										.useTransportSecurity()
										.build();
		
		/*
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 1101)
		            .usePlaintext()
		            .build();
		*/     

		        try {
		        	
		        	HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel).withCallCredentials(callCreds);
		        
		        	HelloResponse helloResponse = stub.hello(HelloRequest.newBuilder()
				            .setFirstName("Baeldung")
				            .setLastName("gRPC")
				            .build());
			        System.out.println("Response received from server:\n" + helloResponse);
			       		        	
		        }
		        catch(Exception e) {
		        	e.printStackTrace();
		        }
				finally {
			        channel.shutdown();
				}
	}

}
