package com.image.imagebench.mapper;

import com.image.imagebench.entity.ServiceStartTimeEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ServiceStartTimeMapper
 *
 * @Author litianwei
 * @Date 2024/1/8
 **/
@Repository
public interface ServiceStartTimeMapper {

    int insertStartTimeResults(List<ServiceStartTimeEntity> entities);
}
