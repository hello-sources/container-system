package com.image.analysis.service;

import java.util.List;
import java.util.Map;

public interface DebianAnalysisService {

    /**
     * 查询容器镜像所有的dpkg包依赖
     **/
    List<String> getAllDpkgLists(String containerID);

    /**
     * 将查询到的容器dpkg保存到文件中
     **/
    Boolean writeListToFile(List<String> dpkgs, String containerID);

    /**
     * 查询dpkg包之间的依赖关系
     **/
    Map<String, List<String>> queryDependencies(String dpkg, String containerID);

    /**
     * 构建镜像自带相关dpkg包之间的依赖关系
     **/
    Map<String, List<String>> buildOriginDependencies(List<String> dpkgs, String containerID);






    /**
     * 使用commit导出为新的镜像
     **/
    Boolean commitToImage(String containerID, String imageName, String tag);

    /**
     * 使用export导出为tar格式镜像
     **/
    Boolean exportToTarImage(String containerID, String imageName, String tag, String path);

    /**
     * 使用import导入镜像
     **/
    Boolean importTarToImage(String path, String sourTarImageName, String destImageName, String destTag);
}
