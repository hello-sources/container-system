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
     * 查询单个dpkg包及其关联依赖关系
     **/
    Map<String, List<String>> querySingleDpkgDependency(String containerID, String dpkgName);

    /**
     * 查询所有可执行文件路径，查询依赖的rpm包
     **/
    List<String> queryMultipleFileDependencies(String containerID, List<String> filePaths);

    /**
     * 查询是否存在以应用名称安装的dpkg软件包
     **/
    List<String> queryInstalledDpkgByAPPName(String containerID, String appName);

    /**
     * 列出所有需要保留的dpkg包
     **/
    List<String> listNeedKeepDpkgs(String containerID, List<String> filePaths, String appName);

    /**
     * 根据手动优化的经验，需要保留的BaseOS的dpkg包
     **/
    List<String> keepReservationDependencies(String containerID, String reservationFile);

    /**
     * 列出所有需要保留的dpkg包
     **/
    List<String> listNeedDeleteDpkgs(String containerID, List<String> keepDpkgs);

    /**
     * 删除无关联的dpkg
     **/
    Boolean deleteDpkgDependencies(String containerID, List<String> deleteRpmList);




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
