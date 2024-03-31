package com.image;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@MapperScan({"com.image.*.mapper"})
@ComponentScan({"com.image.imagebench", "com.image.analysis"})
@SpringBootApplication
@EnableCaching
public class SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
        System.out.println("Application run!");
    }

}
