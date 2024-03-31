package com.image.dedup.service.impl;

import com.image.dedup.entity.BloomFilterEntity;
import com.image.dedup.mapper.BloomFilterMapper;
import com.image.dedup.service.DedupService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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

        return null;
    }

    @Override
    public String queryFingerprintInfo(String bucket, String hash) {
        return null;
    }

    @Override
    public Boolean deleteFingerprintInfo(String bucket, String hash) {
        return null;
    }

    @Override
    public Boolean bloomFilterFindFp(String fingerPrint, String bloomName) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(bloomName);
        return bloomFilter.contains(fingerPrint);
    }

}
