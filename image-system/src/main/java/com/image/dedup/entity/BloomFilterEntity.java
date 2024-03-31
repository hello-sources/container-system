package com.image.dedup.entity;

import lombok.Data;
import javax.persistence.Table;

/**
 * BloomFilterEntity
 *
 * @Author litianwei
 * @Date 2024/3/31
 **/
@Data
@Table(name = "bloom_filters")
public class BloomFilterEntity {
    /**
     * 主键ID
     */
    private int id;

    /**
     * 布隆过滤器名称
     */
    private String bloomFilterName;

    /**
     * 布隆过滤器容量
     */
    private Long expectedInsertions;

    /**
     * 误判率
     */
    private Double fpp;
}
