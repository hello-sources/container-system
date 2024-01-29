package com.image.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.nio.charset.Charset;

/**
 * IndexOptimizeTest
 *
 * @Author litianwei
 * @Date 2024/1/25
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class IndexOptimizeTest {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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

    // 测试使用Json格式数据
    @Test
    public void testJsonObject() throws JSONException {
        //创建JSON对象
        JSONObject jsonObject = new JSONObject();
        Object obj = null;
        //像JSON对象中添加数据
        jsonObject.put("name", "张三");
        jsonObject.put("age", 20);
        jsonObject.put("birth", "1998-01-01");
        jsonObject.put("haveCar", obj);
        jsonObject.put("hasGirlfriend", true);
        jsonObject.put("likes", new String[]{"看电影", "看书"});
        //将JSON对象以字符串的形式打印
        System.out.println(jsonObject.toString());
        System.out.println(JSON.toJSONString(jsonObject));
    }

    // 测试在redis中操作Json格式数据
    @Test
    public void testRedisJsonTemplate() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("size", "8KB");
            jsonObject.put("loc", "/root/software/chunks");
            jsonObject.put("offset", "2024520");
            jsonObject.put("镜像名", "测试镜像名");
            String s = JSON.toJSONString(jsonObject);
            System.out.println(s);
            this.stringRedisTemplate.opsForValue().set("chunkInfo-1", s);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("向Redis中存值报错");
        }
        // 获取值
        Object chunkInfo = stringRedisTemplate.opsForValue().get("chunkInfo-1");
        System.out.println(chunkInfo.toString());
    }

    // 测试使用guava布隆过滤器
    @Test
    public void testBloomFilter() {
        BloomFilter<CharSequence> bloomFilter  = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()),1000,0.0000001);
        bloomFilter.put("abc");
        boolean  isContains = bloomFilter.mightContain("abcd");
        System.out.println(isContains );
    }

}



