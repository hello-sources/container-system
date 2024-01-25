package com.image.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * RedisTest
 *
 * @Author litianwei
 * @Date 2024/1/25
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 测试使用集成Redis
    @Test
    public void testRedisTemplate() {
        // 设置键值对
        redisTemplate.opsForValue().set("测试key", "测试value");

        // 获取值
        String value = (String) redisTemplate.opsForValue().get("测试key");
        System.out.println(value);

        // 删除键值对
        redisTemplate.delete("测试key");
    }


}
