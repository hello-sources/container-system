package com.image.imagebench.entity;

import lombok.Data;

import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * ServiceStartTimeEntity
 *
 * @Author litianwei
 * @Date 2024/1/8
 **/
@Data
@Table(name = "service_start_time")
public class ServiceStartTimeEntity {
    /**
     * 主键ID
     */
    private int id;

    /**
     * 应用名称
     */
    private String app_name;

    /**
     * 应用服务启动命令
     */
    private String start_command;

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
    private BigDecimal start_time;
}
