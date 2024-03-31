package com.image.dedup.mapper;

import com.image.dedup.entity.BloomFilterEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloomFilterMapper {

    List<BloomFilterEntity> queryAllBloomFilterName();

    int insertBloomFilter(List<BloomFilterEntity> bloomFilterEntity);
}
