package com.transformertwin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class TransformerTwinApplication {
 public static void main(String[] args) { SpringApplication.run(TransformerTwinApplication.class, args); }
}
