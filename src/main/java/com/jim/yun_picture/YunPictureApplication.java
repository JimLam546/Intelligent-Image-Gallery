package com.jim.yun_picture;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.jim.yun_picture.mapper")
public class YunPictureApplication {

    public static void main(String[] args) {
        SpringApplication.run(YunPictureApplication.class, args);
    }

}
