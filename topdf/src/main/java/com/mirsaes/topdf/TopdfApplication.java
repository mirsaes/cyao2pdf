package com.mirsaes.topdf;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TopdfApplication {

	@Value("${upload.dir}")
	private String uploadDir;
	
	public static void main(String[] args) {
		
		SpringApplication.run(TopdfApplication.class, args);
	}
	
	@Bean
    CommandLineRunner init() {
        return (String[] args) -> {
            new File(uploadDir).mkdir();
        };
    }	
}
