package com.image.imagebench.service;


public interface ImageBench {

    /**
     * 测试由镜像创建容器时间，单位秒
     **/
    Long getContainerCreateTime(String imageName, String tag);


    /**
     * 测试由容器启动服务时间，单位秒
     **/
    Float getContainerStartTime(String imageName, String tag, String startPath);

}
