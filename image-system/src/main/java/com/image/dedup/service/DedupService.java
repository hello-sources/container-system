package com.image.dedup.service;

import com.image.dedup.entity.BloomFilterEntity;
import org.redisson.api.RBloomFilter;

import java.util.List;

public interface DedupService {

    // 创建布隆过滤器
    RBloomFilter<Object> createBloomFilter(BloomFilterEntity entity);

    // 创建布隆过滤器组
    List<RBloomFilter<Object>> createBloomFilterGroup(List<BloomFilterEntity> blooms);

    // 根据布隆过滤器查询相应的指纹信息
    String queryFingerprintInfo(String bucket, String hash);

    // 删除相关指纹信息
    Boolean deleteFingerprintInfo(String bucket, String hash);

    // 使用布隆过滤器优化指纹索引
    Boolean bloomFilterFindFp(String fingerPrint, String bloomName);

}
