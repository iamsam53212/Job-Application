package com.jobapplication.org;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class JobApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobApplication.class, args);
	}

//	@Component
//	class Startup implements CommandLineRunner{
//
//		@Value("${value}")
//		String value;
//		@Override
//		public void run(String... args) throws Exception {
//			System.out.println("value: "+value);
//		}
//	}


}
