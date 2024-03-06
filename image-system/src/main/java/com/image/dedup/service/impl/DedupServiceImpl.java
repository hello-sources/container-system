package com.image.dedup.service.impl;

import com.image.dedup.service.DedupService;
import org.redisson.api.RBloomFilter;

/**
 * DedupServiceImpl
 *
 * @Author litianwei
 * @Date 2024/1/26
 **/
public class DedupServiceImpl implements DedupService {

    @Override
    public Boolean bloomFilterFindFp(String fingerPrint) {
        return null;
    }

    @Override
    public RBloomFilter<Object> createBloomFilter(Integer expectedInsertions, Double fpp) {
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


}
