package com.example.zaitian.CSE416server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class Cse416ServerApplication {

	public static void main(String[] args) {

		SpringApplication.run(Cse416ServerApplication.class, args);


		System.out.println("Hi Mou");
	}

}
