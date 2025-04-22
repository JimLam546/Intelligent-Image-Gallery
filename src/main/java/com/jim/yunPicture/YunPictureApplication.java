package com.jim.yunPicture;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.jim.yunPicture.mapper")
public class YunPictureApplication {

    public static void main(String[] args) {
        SpringApplication.run(YunPictureApplication.class, args);
    }

}
