package com;

import com.example.filedemo.property.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(
		{FileStorageProperties.class}
)
@SpringBootApplication
public class guyujungApplication {

	public static void main(String[] args) {
		SpringApplication.run(guyujungApplication.class, args);
	}

}
