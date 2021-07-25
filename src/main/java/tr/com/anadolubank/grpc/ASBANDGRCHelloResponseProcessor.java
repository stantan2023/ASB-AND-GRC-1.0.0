package tr.com.anadolubank.grpc;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import tr.com.anadolubank.grpc.proto.HelloResponse;

public class ASBANDGRCHelloResponseProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		
	  Message inMessage = exchange.getIn();
	  String operationName = inMessage.getHeader(org.apache.camel.component.grpc.GrpcConstants.GRPC_METHOD_NAME_HEADER,String.class);

	  System.out.println("Operation Name:"+operationName);
	  
	  HelloResponse response = HelloResponse.newBuilder()
	            .setGreeting("SERKAN")
	            .build();
	  
	  inMessage.setBody(response);
	  
	}

}
