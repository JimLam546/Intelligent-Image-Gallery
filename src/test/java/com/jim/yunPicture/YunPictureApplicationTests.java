package com.jim.yunPicture;

import cn.hutool.core.date.DateUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
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

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedis() {
        redisTemplate.opsForValue().set("test", "test");
        System.out.println(redisTemplate.opsForValue().get("test"));
        Assertions.assertEquals("test", redisTemplate.opsForValue().get("test"));
        redisTemplate.delete("test");
    }

}
