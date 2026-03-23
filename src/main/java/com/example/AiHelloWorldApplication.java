package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        exclude = org.springframework.ai.autoconfigure.chat.client.ChatClientAutoConfiguration.class
)
@MapperScan("com.example.mapper")
public class AiHelloWorldApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiHelloWorldApplication.class, args);
    }

}
