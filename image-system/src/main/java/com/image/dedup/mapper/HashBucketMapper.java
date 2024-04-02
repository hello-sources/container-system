package com.image.dedup.mapper;

import com.image.dedup.entity.HashBucketEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HashBucketMapper {

    List<String> queryAllByBucketName();

    int insertHashBucket(List<HashBucketEntity> hashBucketEntities);

}
