package com.tanmay.spring.SpringBootCircuiteBreaker_Retry;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableRetry
@EnableCircuitBreaker
@EnableHystrixDashboard
@SpringBootApplication
public class SpringBootCircuiteBreakerRetryApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootCircuiteBreakerRetryApplication.class, args);
	}

	@RestController
	public class RestEndPoint {

		private final ServiceThatFails_Hystrix serviceThatFails_Hystrix;
		private final ServiceThatFails_Retry serviceThatFails_retry;

		public RestEndPoint(
		        ServiceThatFails_Hystrix serviceThatFails_Hystrix,
                            ServiceThatFails_Retry serviceThatFails_retry){
			this.serviceThatFails_Hystrix = serviceThatFails_Hystrix;
			this.serviceThatFails_retry = serviceThatFails_retry;
		}

		@GetMapping(value = "/endpointToTestHystrix")
		public int callEndPoint1() throws Exception {
			return serviceThatFails_Hystrix.callBackEndServiceWhichFails_hystrix();
		}

        @GetMapping(value = "/endpointToTestRetry")
        public int callEndPoint2() throws Exception {
            return serviceThatFails_retry.callBackEndServiceWhichFails_HystrixWithRetry();
        }
	}

	class BackEndFailedException extends RuntimeException{
        public BackEndFailedException(String message) {
            super(message);
        }
    }

	@Service
	class ServiceThatFails_Hystrix {

		public int callFallbackMethod(){
			return 2;
		}

		@HystrixCommand(fallbackMethod = "callFallbackMethod")
		public int callBackEndServiceWhichFails_hystrix() throws Exception {
			if(Math.random() > .5){
				Thread.sleep(1000);
                throw new BackEndFailedException("Back End Failed");
			}
			return 1;
		}
	}

    @Service
    class ServiceThatFails_Retry {
	    int retryCount = 0;

	    @Recover
        public int callFallbackMethod(BackEndFailedException ex){
	        retryCount = 0;
	        return 2;
        }

        @Retryable(include = BackEndFailedException.class,
                        maxAttempts = 4)
        public int callBackEndServiceWhichFails_HystrixWithRetry() throws Exception {

	        if(retryCount == 0)
	            retryCount ++;
	        if(retryCount == 1)
                System.out.println("First Retry");retryCount ++;
            if(retryCount == 2)
                System.out.println("Second Retry");retryCount ++;
            if(retryCount == 3)
                System.out.println("Third Retry");retryCount ++;

	        throw new BackEndFailedException("Back End Failed");
        }
    }
}