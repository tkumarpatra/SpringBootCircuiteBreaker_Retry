package com.tanmay.spring.SpringBootCircuiteBreaker_Retry;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableCircuitBreaker
@EnableHystrixDashboard
@SpringBootApplication
public class SpringBootCircuiteBreakerRetryApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootCircuiteBreakerRetryApplication.class, args);
	}

	@RestController
	public class RestEndPoint {

		private final ServiceThatFails serviceThatFails;
		public RestEndPoint(ServiceThatFails serviceThatFails){
			this.serviceThatFails = serviceThatFails;
		}

		@GetMapping(value = "/iamendpoint")
		public int callEndPoint() throws Exception {
			return serviceThatFails.callBackEndServiceWhichFails();
		}
	}

	@Service
	class ServiceThatFails{

		public int callFallbackMethod(){
			return 2;
		}

		@HystrixCommand(fallbackMethod = "callFallbackMethod")
		public int callBackEndServiceWhichFails() throws Exception {
			if(Math.random() > .5){
				Thread.sleep(1000);
				throw new RuntimeException();
			}
			return 1;
		}
	}
}