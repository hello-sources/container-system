package com.image.imagebench.service;


public interface ImageBench {

    /**
     * 测试由镜像创建容器时间，单位毫秒
     **/
    Long getContainerCreateTime(String imageName, String tag);


    /**
     * 测试由容器启动应用服务时间，单位毫秒
     **/
    Long getServiceStartTime(String containerID, String startPath);

}
