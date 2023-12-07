package com.image.analysis.service;

import java.util.List;

public interface AnalysisService {

    /**
     * 查询容器镜像所有的rpm包依赖
     **/
    List<String> getAllRpmLists(String containerID);

    /**
     * 将查询到的容器rpm保存到文件中
     **/
    void writeListToFile(List<String> rpms, String containerID);

    /**
     * 构建镜像自带相关rpm包之间的依赖关系
     **/
    void buildOriginDependencies(List<String> rpms, String containerID);

}