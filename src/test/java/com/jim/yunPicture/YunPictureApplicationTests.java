package com.jim.yunPicture;

import cn.hutool.core.date.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.Date;

@SpringBootTest
class YunPictureApplicationTests {

    @Test
    void contextLoads() throws IOException {
        String property = System.getProperty("user.dir");
        File tempFile = File.createTempFile(property, null);
        System.out.println("123");
    }

    public static void main(String[] args) throws IOException {
        System.out.println(DateUtil.format(new Date(), "yyyyMMddHHmmss"));
    }

}
