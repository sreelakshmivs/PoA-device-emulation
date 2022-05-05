package com.example.poadevice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PoaDeviceApplication implements ApplicationRunner {

	@Value("${server.port}")
	private int port;

    public static void main(String[] args) {
        SpringApplication.run(PoaDeviceApplication.class, args);
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        System.out.println("Open UI: http://127.0.0.1:" + port + "/index.html");
    }

}
