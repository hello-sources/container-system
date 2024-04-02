package com.image.dedup.service;

import com.alibaba.fastjson.JSONObject;
import com.image.dedup.entity.BloomFilterEntity;
import com.image.dedup.entity.HashBucketEntity;
import org.redisson.api.RBloomFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DedupService {

    // 创建布隆过滤器
    RBloomFilter<Object> createBloomFilter(BloomFilterEntity entity);

    // 创建布隆过滤器组
    List<RBloomFilter<Object>> createBloomFilterGroup(List<BloomFilterEntity> blooms);

    // 向布隆过滤器插入数据
    Boolean insertIntoBloom(String bloomFilterName, String fingerPrint);

    // 使用布隆过滤器优化指纹索引
    Boolean bloomFilterFindFp(String fingerPrint, String bloomName);

    // 查询所有的哈希桶
    Set<String> queryAllHashBucket();

    // 删除哈希桶
    Boolean destroyHashBucket(String bucketName);

    // 建立哈希桶与布隆过滤器映射关系，查询哈希桶信息
    List<String> queryHashBucketName();

    // 将哈希桶与布隆过滤器映射关系写入数据库
    int insertHashBucketInfo(List<HashBucketEntity> hashBucketEntities);

    // 根据哈希值查询相关元数据信息
    JSONObject queryFingerprintInfo(String bucketName, String hash);

    // 添加相关元数据信息
    void addMetaInfoToBucket(String bucketName, JSONObject jsonObject);

    // 删除相关元数据信息
    Boolean deleteFingerprintInfo(String bucketName, String hash) throws IOException;

}
