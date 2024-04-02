package com.image.dedup.entity;

import lombok.Data;

import javax.persistence.Table;

/**
 * HashBucketEntity
 *
 * @Author litianwei
 * @Date 2024/4/2
 **/
@Data
@Table(name = "hash_bucket")
public class HashBucketEntity {
    /**
     * 主键ID
     */
    private int id;

    /**
     * 哈希桶名称
     */
    private String bucketName;

    /**
     * 布隆过滤器名称
     */
    private String bloomFilterName;

    /**
     * 布隆过滤器ID
     */
    private int bloomFilterID;

}
