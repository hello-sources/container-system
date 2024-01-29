package com.image.dedup.service;

public interface DedupService {

    // TODO 使用布隆过滤器优化指纹索引
    Boolean bloomFilterFindFp(String fingerPrint);

}
