package com.bridgeit.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
/**
 * <p>
 * <b>Todo App User application main class for providing the services through eureka server.</b>
 * </p>
 * 
 * @author : Vishal Dhar Dubey
 * @version : 1.0
 * @since : 05-08-2018
 */
@SpringBootApplication
@EnableHystrix
@EnableHystrixDashboard
@EnableEurekaClient
public class TodoappuserserviceApplication {
	/**
	 * <p>main function is to start the todoappuserservice</p>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(TodoappuserserviceApplication.class, args);
	}
}
