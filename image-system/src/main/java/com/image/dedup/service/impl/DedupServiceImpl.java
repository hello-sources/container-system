package com.image.dedup.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.image.dedup.entity.BloomFilterEntity;
import com.image.dedup.entity.HashBucketEntity;
import com.image.dedup.mapper.BloomFilterMapper;
import com.image.dedup.mapper.HashBucketMapper;
import com.image.dedup.service.DedupService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DedupServiceImpl
 *
 * @Author litianwei
 * @Date 2024/1/26
 **/
@Slf4j
@Service
public class DedupServiceImpl implements DedupService {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private BloomFilterMapper bloomFilterMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private HashBucketMapper hashBucketMapper;


    @Override
    public RBloomFilter<Object> createBloomFilter(BloomFilterEntity entity) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(entity.getBloomFilterName());
        bloomFilter.tryInit(entity.getExpectedInsertions(), entity.getFpp());
        List<BloomFilterEntity> list = new ArrayList<>();
        list.add(entity);
        int i = bloomFilterMapper.insertBloomFilter(list);
        if (i <= 0) {
            System.out.println("创建布隆过滤器失败");
        }
        return bloomFilter;
    }

    @Override
    public List<RBloomFilter<Object>> createBloomFilterGroup(List<BloomFilterEntity> blooms) {
        List<RBloomFilter<Object>> ans = new ArrayList<>();
        for (BloomFilterEntity entity : blooms) {
            ans.add(createBloomFilter(entity));
        }
        return ans;
    }

    @Override
    public Boolean insertIntoBloom(String bloomFilterName, String fingerPrint) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter("test_bloomFilter1");
        return bloomFilter.add(fingerPrint);
    }

    @Override
    public Boolean bloomFilterFindFp(String fingerPrint, String bloomName) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(bloomName);
        return bloomFilter.contains(fingerPrint);
    }

    @Override
    public Set<String> queryAllHashBucket() {
        Set<String> buckets = new HashSet<>();
        // 使用scan代替keys命令来避免性能问题
        Cursor<byte[]> cursor = stringRedisTemplate.getConnectionFactory().getConnection().scan(
            ScanOptions.scanOptions().match("*").build());
        while (cursor.hasNext()) {
            buckets.add(new String(cursor.next()));
        }
        return buckets;
    }


    @Override
    public Boolean destroyHashBucket(String bucketName) {
        if (!stringRedisTemplate.hasKey(bucketName)) return false;
        return stringRedisTemplate.delete(bucketName);
    }

    @Override
    public List<String> queryHashBucketName() {
        List<String> strings = hashBucketMapper.queryAllByBucketName();
        return strings;
    }

    @Override
    public int insertHashBucketInfo(List<HashBucketEntity> hashBucketEntities) {
        return hashBucketMapper.insertHashBucket(hashBucketEntities);
    }

    @Override
    public JSONObject queryFingerprintInfo(String bucketName, String hash) {
        try {
            List<String> jsonList = this.stringRedisTemplate.opsForList().range(bucketName, 0, -1);
            for (String json : jsonList) {
                JSONObject jsonObject = JSONObject.parseObject(json);
                if (hash.equals(jsonObject.getString("key"))) {
                    return jsonObject;
                }
            }
        } catch (Exception e) {
            System.out.println("向Redis中取值报错");
        }
        return null;
    }

    @Override
    public void addMetaInfoToBucket(String bucketName, JSONObject jsonObject) {
        try {
            this.stringRedisTemplate.opsForList().rightPush(bucketName, jsonObject.toJSONString());
        } catch (Exception e) {
            System.out.println("向Redis中存值报错");
        }
    }

    @Override
    public Boolean deleteFingerprintInfo(String bucketName, String hash) throws IOException {
        Boolean ans = false;
        try {
            ListOperations<String, String> listOps = this.stringRedisTemplate.opsForList();
            List<String> jsonList = listOps.range(bucketName, 0, -1);
            for (String json : jsonList) {
                JSONObject jsonObject = JSONObject.parseObject(json);
                if (hash.equals(jsonObject.getString("key"))) {
                    listOps.remove("multi-json-list", 0, json);
                    ans = true;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("删除元数据报错");
        }
        return ans;
    }

}
