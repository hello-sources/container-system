package com.image.imagebench.mapper;

import com.image.imagebench.entity.ContainerCreateTimeEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ImageBenchMapper
 *
 * @Author litianwei
 * @Date 2024/1/5
 **/
@Repository
public interface ImageBenchMapper {

    int insertResults(List<ContainerCreateTimeEntity> configList);

}
