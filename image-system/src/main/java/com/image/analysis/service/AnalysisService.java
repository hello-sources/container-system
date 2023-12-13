package com.image.analysis.service;

import java.util.List;
import java.util.Map;

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
     * 存储简化后的容器rpm保存到文件中
     **/
    void writeSimpleListToFile(List<String> rpms, String containerID);

    /**
     * 构建镜像自带相关rpm包之间的依赖关系
     **/
    Map<String, List<String>> buildOriginDependencies(List<String> rpms, String containerID);

    /**
     * 查询rpm包之间的依赖关系
     **/
    Map<String, String> queryDependencies(String dep, String containerID);

    /**
     * 根据依赖关系生成.dot数据，并绘制可视化图
     **/
    Boolean drawDependenciesTopology(Map<String, List<String>> deps);

    /**
     * 根据单个可执行文件路径，查询依赖的rpm包
     **/
    List<String> querySingleFileDependency(String containID, String filePath);

    /**
     * 查询所有可执行文件路径，查询依赖的rpm包
     **/
    List<String> queryAllFileDependencies(String containerID, List<String> filePaths);

    /**
     * 查询单个rpm包及其关联依赖关系
    **/
    Map<String, List<String>> querySingleRpmDependency(String containerID, String rpmName);

}
