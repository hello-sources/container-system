package com.image.dedup.service;

import org.redisson.api.RBloomFilter;

public interface DedupService {

    // TODO 使用布隆过滤器优化指纹索引
    Boolean bloomFilterFindFp(String fingerPrint);

    // 创建布隆过滤器
    RBloomFilter<Object> createBloomFilter(Integer expectedInsertions, Double fpp);

    // 根据布隆过滤器查询相应的指纹信息
    String queryFingerprintInfo(String bucket, String hash);

    // 删除相关指纹信息
    Boolean deleteFingerprintInfo(String bucket, String hash);

}
