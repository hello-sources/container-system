package com.image.imagebench.entity;

import lombok.Data;

import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * ContainerCreateTimeEntity
 *
 * @Author litianwei
 * @Date 2024/1/5
 **/
@Data
@Table(name = "container_create_time")
public class ContainerCreateTimeEntity {
    /**
     * 主键ID
     */
    private int id;

    /**
     * 镜像名称
     */
    private String image_name;

    /**
     * 镜像tag
     */
    private String image_tag;

    /**
     * 镜像ID
     */
    private String image_id;

    /**
     * 镜像大小
     */
    private String image_size;

    /**
     * 容器ID
     */
    private String container_id;

    /**
     * 创建容器时间
     */
    private BigDecimal create_time;
}
