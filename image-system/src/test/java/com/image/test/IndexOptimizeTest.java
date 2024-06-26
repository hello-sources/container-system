package com.image.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.image.dedup.entity.BloomFilterEntity;
import com.image.dedup.entity.HashBucketEntity;
import com.image.dedup.mapper.BloomFilterMapper;
import com.image.dedup.service.impl.DedupServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private BloomFilterMapper bloomFilterMapper;

    @Resource
    private DedupServiceImpl dedupServiceImpl;

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
            this.stringRedisTemplate.opsForValue().set("chunkInfo-2", s);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("向Redis中存值报错");
        }
        // 获取值
        Object chunkInfo = stringRedisTemplate.opsForValue().get("chunkInfo-2");
        System.out.println(chunkInfo.toString());
    }

    // 测试使用guava布隆过滤器
    @Test
    public void testBloomFilter() {
        /** 预计插入的数据 */
        Integer expectedInsertions = 1000000;

        // BloomFilter<Integer> bloomFilter  = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()),
        //     1000,0.00000001);
        BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), 1000000, 0.00000000000001);
        // 布隆过滤器增加元素
        for (Integer i = 0; i < expectedInsertions; i++) {
            bloomFilter.put(i);
        }
        // 统计元素
        int count = 0;
        for (Integer i = expectedInsertions; i < expectedInsertions * 2; i++) {
            if (bloomFilter.mightContain(i)) {
                count++;
            }
        }
        System.out.println("误判次数" + count);
    }

    // 测试使用redisson创建布隆过滤器
    @Test
    public void testRedisson() {

        /** 预计插入的数据 */
        Integer expectedInsertions = 10000;
        /** 误判率 */
        Double fpp = 0.0000001;

        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter("user-redisson");
        bloomFilter.tryInit(expectedInsertions, fpp);

        // 布隆过滤器增加元素
        for (Integer i = 0; i < expectedInsertions; i++) {
            bloomFilter.add(i);
        }

        // 统计元素
        int count = 0;
        for (int i = expectedInsertions; i < expectedInsertions * 2; i++) {
            if (bloomFilter.contains(i)) {
                count++;
            }
        }
        System.out.println("误判次数" + count);
    }

    // 测试使用redisson创建key-value
    @Test
    public void testRedissonBucket() {
        RBucket<Object> bucket1 = redissonClient.getBucket("bucket1");
        bucket1.set("v1234");
        Object o = bucket1.get();
        System.out.println(o.toString());
    }

    // 测试在redis中操作多组JSON格式数据,JSON字符串嵌套
    @Test
    public void testMultiRedisJsonTemplate() {
        List<String> hash = new ArrayList<>();
        hash.add("0B5A80E50078CBC128F222796C325E69C47E1816");
        hash.add("0B9011DFE1631DD5BF4A908492F94D09E357E5BB");
        hash.add("0C0BB1B6C7597DB1401516BF6EE5C833AB5AC5C0");
        hash.add("0CF9E13DD5EEAB68EDC9B620835B02DBEAC47972");
        hash.add("0D6D0BF1CF6F18E2E0CC656D148682353A508C53");
        hash.add("0D6DD30A2829AF0EC5CC4A20A5A553A3861CEAF0");
        hash.add("0D313AE03B4905FF78182B0A98D2D61C6F254ED3");
        hash.add("0DEC4371198560BCE777EF11558FC3A9CEB53B4A");
        hash.add("0E2D6186F68DC3AD86AC0C3711F85F665787E227");
        hash.add("0FDC3297DBFEC7AC1AD86EB5363860B03D9DC95D");
        hash.add("00E3F2A0813D3B00C5499E34F85DC0410D16AE69");
        hash.add("1A5D813F12E4CC5767939C98F37696CE031D954D");

        Map<String, List<String>> map = new HashMap<>();
        map.put("0D313AE03B4905FF78182B0A98D2D61C6F254ED3", new ArrayList<>() {{
            add("/root/ltw1");
            add("4.25MB");
            add("223451");
        }});
        map.put("0DEC4371198560BCE777EF11558FC3A9CEB53B4A", new ArrayList<>() {{
            add("/root/ltw2");
            add("5.25MB");
            add("223452");
        }});
        map.put("0E2D6186F68DC3AD86AC0C3711F85F665787E227", new ArrayList<>() {{
            add("/root/ltw3");
            add("6.25MB");
            add("223453");
        }});
        map.put("0FDC3297DBFEC7AC1AD86EB5363860B03D9DC95D", new ArrayList<>() {{
            add("/root/ltw4");
            add("7.25MB");
            add("223454");
        }});
        map.put("00E3F2A0813D3B00C5499E34F85DC0410D16AE69", new ArrayList<>() {{
            add("/root/ltw5");
            add("8.25MB");
            add("223455");
        }});
        map.put("1A5D813F12E4CC5767939C98F37696CE031D954D", new ArrayList<>() {{
            add("/root/ltw6");
            add("9.99GB");
            add("223456");
        }});
        map.put("0B5A80E50078CBC128F222796C325E69C47E1816", new ArrayList<>() {{
            add("/root/ltw1");
            add("4.25KB");
            add("123451");
        }});
        map.put("0B9011DFE1631DD5BF4A908492F94D09E357E5BB", new ArrayList<>() {{
            add("/root/ltw2");
            add("5.25KB");
            add("123452");
        }});
        map.put("0C0BB1B6C7597DB1401516BF6EE5C833AB5AC5C0", new ArrayList<>() {{
            add("/root/ltw3");
            add("6.25KB");
            add("123453");
        }});
        map.put("0CF9E13DD5EEAB68EDC9B620835B02DBEAC47972", new ArrayList<>() {{
            add("/root/ltw4");
            add("7.25KB");
            add("123454");
        }});
        map.put("0D6D0BF1CF6F18E2E0CC656D148682353A508C53", new ArrayList<>() {{
            add("/root/ltw5");
            add("8.25KB");
            add("123455");
        }});
        map.put("0D6DD30A2829AF0EC5CC4A20A5A553A3861CEAF0", new ArrayList<>() {{
            add("/root/ltw6");
            add("9.25KB");
            add("123456");
        }});


        // StringBuffer stb = new StringBuffer();
        // stb.append("{");
        // for (Map.Entry<String, List<String>> entry : map.entrySet()) {
        //     stb.append("\"").append(entry.getKey()).append("\":{")
        //         .append("\"loc\":\"").append(entry.getValue().get(0)).append("\",")
        //         .append("\"blockSize\":").append(entry.getValue().get(1)).append(",")
        //         .append("\"offset\":").append(entry.getValue().get(2))
        //         .append("},");
        // }
        //
        // // 移除最后一个多余的逗号
        // stb.setLength(stb.length() - 1);
        // stb.append("}");

        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", entry.getKey());
            jsonObject.put("loc", entry.getValue().get(0));
            jsonObject.put("blockSize", entry.getValue().get(1));
            jsonObject.put("offset", entry.getValue().get(2));
            jsonArray.add(jsonObject);
        }

        try {
            this.stringRedisTemplate.opsForValue().append("multi-json", jsonArray.toJSONString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("向Redis中存值报错");
        }

        // 获取值
        Object chunkInfo = stringRedisTemplate.opsForValue().get("multi-json");
        System.out.println(chunkInfo.toString());
    }

    // 使用JSONObject往Redis添加数据，先取出来JSONArray，然后再set回去，比较呆
    @Test
    public void testUseJSONObject() {
        JSONArray jsonArray = JSON.parseArray(this.stringRedisTemplate.opsForValue().get("multi-json"));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        jsonObject.put("loc", "/test/img");
        jsonObject.put("blockSize", "6.66KB");
        jsonObject.put("offset", "123456");
        jsonArray.add(jsonObject);

        try {
            this.stringRedisTemplate.opsForValue().set("multi-json", jsonArray.toJSONString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("向Redis中存值报错");
        }
    }

    // 测试存成List类型数据，方便数据追加
    @Test
    public void testUseListObject() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("0D313AE03B4905FF78182B0A98D2D61C6F254ED3", new ArrayList<>() {{
            add("/root/ltw1");
            add("4.25MB");
            add("223451");
        }});
        map.put("0DEC4371198560BCE777EF11558FC3A9CEB53B4A", new ArrayList<>() {{
            add("/root/ltw2");
            add("5.25MB");
            add("223452");
        }});
        map.put("0E2D6186F68DC3AD86AC0C3711F85F665787E227", new ArrayList<>() {{
            add("/root/ltw3");
            add("6.25MB");
            add("223453");
        }});
        map.put("0FDC3297DBFEC7AC1AD86EB5363860B03D9DC95D", new ArrayList<>() {{
            add("/root/ltw4");
            add("7.25MB");
            add("223454");
        }});
        map.put("00E3F2A0813D3B00C5499E34F85DC0410D16AE69", new ArrayList<>() {{
            add("/root/ltw5");
            add("8.25MB");
            add("223455");
        }});
        map.put("1A5D813F12E4CC5767939C98F37696CE031D954D", new ArrayList<>() {{
            add("/root/ltw6");
            add("9.99GB");
            add("223456");
        }});
        map.put("0B5A80E50078CBC128F222796C325E69C47E1816", new ArrayList<>() {{
            add("/root/ltw1");
            add("4.25KB");
            add("123451");
        }});
        map.put("0B9011DFE1631DD5BF4A908492F94D09E357E5BB", new ArrayList<>() {{
            add("/root/ltw2");
            add("5.25KB");
            add("123452");
        }});
        map.put("0C0BB1B6C7597DB1401516BF6EE5C833AB5AC5C0", new ArrayList<>() {{
            add("/root/ltw3");
            add("6.25KB");
            add("123453");
        }});
        map.put("0CF9E13DD5EEAB68EDC9B620835B02DBEAC47972", new ArrayList<>() {{
            add("/root/ltw4");
            add("7.25KB");
            add("123454");
        }});
        map.put("0D6D0BF1CF6F18E2E0CC656D148682353A508C53", new ArrayList<>() {{
            add("/root/ltw5");
            add("8.25KB");
            add("123455");
        }});
        map.put("0D6DD30A2829AF0EC5CC4A20A5A553A3861CEAF0", new ArrayList<>() {{
            add("/root/ltw6");
            add("9.25KB");
            add("123456");
        }});

        try {
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("key", entry.getKey());
                jsonObject.put("loc", entry.getValue().get(0));
                jsonObject.put("blockSize", entry.getValue().get(1));
                jsonObject.put("offset", entry.getValue().get(2));
                this.stringRedisTemplate.opsForList().rightPush("multi-json-list", jsonObject.toJSONString());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("向Redis中存值报错");
        }


    }

    // 尝试使用List类型，追加程序
    @Test
    public void testAddJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", "yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
        jsonObject.put("loc", "/test/img");
        jsonObject.put("blockSize", "7.77KB");
        jsonObject.put("offset", "654321");

        try {
            this.stringRedisTemplate.opsForList().rightPush("multi-json-list", jsonObject.toJSONString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("向Redis中存值报错");
        }
    }

    // 查询bucket里面指定的JSON字符串
    @Test
    public void tesryJSON() {
        try {
            List<String> jsonList = this.stringRedisTemplate.opsForList().range("multi-json-list", 0, -1);
            for (String json : jsonList) {
                JSONObject jsonObject = JSONObject.parseObject(json);
                if ("yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy".equals(jsonObject.getString("key"))) {
                    System.out.println(jsonObject.toJSONString());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("向Redis中取值报错");
        }
    }

    // 删除bucket里面指定的JSON字符串
    @Test
    public void testRemoveJSON() {
        try {
            ListOperations<String, String> listOps = this.stringRedisTemplate.opsForList();
            List<String> jsonList = listOps.range("multi-json-list", 0, -1);
            for (String json : jsonList) {
                JSONObject jsonObject = JSONObject.parseObject(json);
                if ("yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy".equals(jsonObject.getString("key"))) {
                    listOps.remove("multi-json-list", 0, json);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("删除字符串报错");
        }
    }

    // 查询所有布隆过滤器
    @Test
    public void testQueryAllBloomFilter() {
        List<BloomFilterEntity> bloomFilterEntities = bloomFilterMapper.queryAllBloomFilterName();
        for (int i = 0; i < bloomFilterEntities.size(); i++) {
            System.out.println(bloomFilterEntities.get(i));
        }
    }

    // 插入布隆过滤器信息到数据表中
    @Test
    public void testInsertBloomFilterInfo() {
        List<BloomFilterEntity> list = new ArrayList<>();
        BloomFilterEntity entity1 = new BloomFilterEntity();
        entity1.setBloomFilterName("bloom4");
        entity1.setExpectedInsertions(100000L);
        entity1.setFpp(0.001);
        BloomFilterEntity entity2 = new BloomFilterEntity();
        entity2.setBloomFilterName("bloom5");
        entity2.setExpectedInsertions(1000000L);
        entity2.setFpp(0.0001);
        list.add(entity1);
        list.add(entity2);
        int i = bloomFilterMapper.insertBloomFilter(list);
        System.out.println(i);
    }

    // 测试创建布隆过滤器
    @Test
    public void testCreateBloomFilter() {
        BloomFilterEntity entity = new BloomFilterEntity();
        entity.setBloomFilterName("test_create_bloomFilter");
        entity.setExpectedInsertions(123456L);
        entity.setFpp(0.001);
        RBloomFilter<Object> bloomFilter = dedupServiceImpl.createBloomFilter(entity);
        System.out.println(bloomFilter.contains("132465"));
    }

    // 测试创建布隆过滤器组
    @Test
    public void testCreateBloomFilterGroup() {
        List<BloomFilterEntity> list = new ArrayList<>();
        BloomFilterEntity entity = new BloomFilterEntity();
        entity.setBloomFilterName("test_bloomFilter1");
        entity.setExpectedInsertions(100000L);
        entity.setFpp(0.001);
        BloomFilterEntity entity1 = new BloomFilterEntity();
        entity1.setBloomFilterName("test_bloomFilter2");
        entity1.setExpectedInsertions(100000L);
        entity1.setFpp(0.001);
        list.add(entity);
        list.add(entity1);
        List<RBloomFilter<Object>> bloomFilterGroup = dedupServiceImpl.createBloomFilterGroup(list);
        System.out.println(bloomFilterGroup.size());
    }

    // 测试向布隆过滤器中添加和查询元素
    @Test
    public void testQueryAndAddInfo() {
        String fingerPrint = "0x123fabcdef456789";
        String bloomFilterName = "test_bloomFilter1";
        Boolean aBoolean = dedupServiceImpl.bloomFilterFindFp(fingerPrint, bloomFilterName);
        if (aBoolean) {
            System.out.println("查询：" + fingerPrint + " 成功");
        } else {
            System.out.println("查询：" + fingerPrint + " 失败");
        }
        Boolean aBoolean1 = dedupServiceImpl.insertIntoBloom(bloomFilterName, fingerPrint);
        if (!aBoolean1) {
            System.out.println("插入数据失败");
        }
        Boolean aBoolean2 = dedupServiceImpl.bloomFilterFindFp(fingerPrint, bloomFilterName);
        if (aBoolean2) {
            System.out.println("查询：" + fingerPrint + " 成功");
        } else {
            System.out.println("查询：" + fingerPrint + " 失败");
        }
    }

    // 查询bucket里面对应哈希值的元数据信息
    @Test
    public void testQeuryMetaInfoFromBucket() {
        String bucketName = "multi-json-list";
        String hash = "0C0BB1B6C7597DB1401516BF6EE5C833AB5AC5C0";
        JSONObject jsonObject = dedupServiceImpl.queryFingerprintInfo(bucketName, hash);
        System.out.println(jsonObject.get("key"));
        System.out.println(jsonObject.get("loc"));
        System.out.println(jsonObject.get("offset"));
        System.out.println(jsonObject.get("blockSize"));
    }

    // 测试删除bucket里面对应哈希值的元数据信息
    @Test
    public void testDeleteInfoFromBucket() throws IOException {
        String bucketName = "multi-json-list";
        String hash = "0CF9E13DD5EEAB68EDC9B620835B02DBEAC47972";
        Boolean res = dedupServiceImpl.deleteFingerprintInfo(bucketName, hash);
        if (res) {
            System.out.println("删除元数据成功");
        } else {
            System.out.println("删除元数据失败");
        }
    }

    // 尝试向布隆过滤器添加元素
    @Test
    public void testAddMetaInfoToBucket() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", "lovelovelovelovelovelovelove");
        jsonObject.put("loc", "/ltw/ltw");
        jsonObject.put("blockSize", "5.20KB");
        jsonObject.put("offset", "1111111");

        String bucketName = "multi-json-list";
        dedupServiceImpl.addMetaInfoToBucket(bucketName, jsonObject);
    }

    // 查询所有的哈希桶
    @Test
    public void testQueryAllBuckets() {
        Set<String> set = dedupServiceImpl.queryAllHashBucket();
        for (String str : set) {
            System.out.println(str);
        }
    }

    // 测试删除哈希桶
    @Test
    public void testDeleteHashBucket() {
        stringRedisTemplate.opsForHash().put("testHash", "value1", "value2");
        System.out.println(stringRedisTemplate.opsForHash().get("testHash", "value1"));
        Boolean testHash = dedupServiceImpl.destroyHashBucket("testHash");
        if (testHash) {
            System.out.println("删除bucket成功");
        } else {
            System.out.println("删除bucket失败");
        }
    }

    // 从数据库中查询哈希桶信息
    @Test
    public void testQueryAllHashBucketInfo() {
        List<String> strings = dedupServiceImpl.queryHashBucketName();
        for (String name : strings) {
            System.out.println(name);
        }
    }

    // 向数据库中插入哈希桶信息
    @Test
    public void testInsertHashBucketInfo() {
        List<HashBucketEntity> hashBucketEntities = new ArrayList<>();
        HashBucketEntity entity1 = new HashBucketEntity();
        entity1.setBucketName("testBucket1");
        entity1.setBloomFilterID(3);
        entity1.setBloomFilterName("testBloomFilter1");
        HashBucketEntity entity2 = new HashBucketEntity();
        entity2.setBucketName("testBucket2");
        entity2.setBloomFilterID(4);
        entity2.setBloomFilterName("testBloomFilter3");
        hashBucketEntities.add(entity1);
        hashBucketEntities.add(entity2);
        int i = dedupServiceImpl.insertHashBucketInfo(hashBucketEntities);
        System.out.println("插入了 " + i + " 条数据");

    }

}