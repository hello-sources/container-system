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
     * 构建镜像自带相关dpkg包之间的依赖关系，使用dpkg原生方式，但是会缺少相关依赖项
     **/
    Map<String, List<String>> buildOriginDependencies_absent(String containerID);

    /**
     * 构建镜像自带相关dpkg包之间的依赖关系
     **/
    Map<String, List<String>> buildOriginDependencies(List<String> dpkgs, String containerID);

    /**
     * 根据依赖关系生成.dot数据，并绘制可视化图
     **/
    Boolean drawDependenciesTopology(Map<String, List<String>> deps);

    /**
     * 查询单个Dpkg包及其关联依赖关系
     **/
    Map<String, List<String>> querySingleDpkgDependency(String containerID, String rpmName);



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
