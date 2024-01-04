package com.image.test;

import com.image.analysis.service.impl.DebianAnalysisServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DebianAnalysisTest
 *
 * @Author litianwei
 * @Date 2023/12/30
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class DebianAnalysisTest {

    @Autowired
    public DebianAnalysisServiceImpl debianAnalysisServiceImpl;

    // 测试获取所有的dpkg包依赖
    @Test
    public void testGetAllDpkgLists() {
        List<String> allDpkgLists = debianAnalysisServiceImpl.getAllDpkgLists("112d40ae881e");
        if (allDpkgLists.isEmpty()) {
            System.out.println("----获取dpkg包失败----");
        }
        System.out.println("----dpkg size: " + allDpkgLists.size() + "----");
        for (String str : allDpkgLists) {
            System.out.println(str);
        }
        return ;
    }

    // 测试将获得的的dpkg包写入文件
    @Test
    public void testWriteListToFile() {
        List<String> allDpkgLists = debianAnalysisServiceImpl.getAllDpkgLists("112d40ae881e");
        Boolean res = debianAnalysisServiceImpl.writeListToFile(allDpkgLists, "112d40ae881e");
        if (res) {
            System.out.println("----dpkg包列表写入文件成功----");
        } else {
            System.out.println("----dpkg包列表写入文件失败----");
        }
    }

    // 测试获取单个dpkg包的依赖，包含Depends和Pre_Depends
    @Test
    public void testQueryDependencies() {
        // libperl5.24、perl、tar之类的
        Map<String, List<String>> dpkgDepMap = debianAnalysisServiceImpl
            .queryDependencies("mysql-community-server", "112d40ae881e");

        for (Map.Entry<String, List<String>> entry : dpkgDepMap.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            System.out.println("key : " + key + ", value : " + value);
        }
    }

    // 测试使用原生方式获取全部dpkg包的依赖
    @Test
    public void testBuildOriginDependencies_absent() {
        Map<String, List<String>> stringListMap = debianAnalysisServiceImpl
            .buildOriginDependencies_absent("112d40ae881e");
        System.out.println("map size : " + stringListMap.size());
        for (Map.Entry<String, List<String>> entry : stringListMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            System.out.println("key : " + key + ", value : " + value);
        }
        return ;
    }

    // 测试绘制dpkg相关依赖关系拓扑图，原生方式可能会缺乏相关依赖项
    @Test
    public void testDrawDependenciesTopology_absent() {
        Map<String, List<String>> dpkgMap = debianAnalysisServiceImpl
            .buildOriginDependencies_absent("112d40ae881e");
        Boolean res = debianAnalysisServiceImpl.drawDependenciesTopology(dpkgMap);
        if (res) {
            System.out.println("draw dpkg dependencies succeed");
        } else {
            System.out.println("draw dpkg dependencies failed");
        }
    }

    // 测试构建dpkg相关依赖关系拓扑图
    @Test
    public void testBuildOriginDependencies() {
        List<String> allDpkgLists = debianAnalysisServiceImpl.getAllDpkgLists("112d40ae881e");
        Map<String, List<String>> dpkgMap = debianAnalysisServiceImpl
            .buildOriginDependencies(allDpkgLists, "112d40ae881e");
        System.out.println("map size : " + dpkgMap.size());
        for (Map.Entry<String, List<String>> entry : dpkgMap.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue().toString();
            System.out.println(key + " -> " + val);
        }
    }

    // 测试绘制全部dpkg依赖拓扑图
    @Test
    public void testDrawDependenciesTopology() {
        List<String> allDpkgLists = debianAnalysisServiceImpl.getAllDpkgLists("112d40ae881e");
        Map<String, List<String>> dpkgMap = debianAnalysisServiceImpl
            .buildOriginDependencies(allDpkgLists, "112d40ae881e");
        Boolean res = debianAnalysisServiceImpl.drawDependenciesTopology(dpkgMap);
        if (res) {
            System.out.println("构建dpkg依赖拓扑成功");
        } else {
            System.out.println("构建dpkg依赖拓扑失败");
        }
    }

    // 测试以单个dpkg构建依赖拓扑
    @Test
    public void testQuerySingleDpkgDependency() {
        Map<String, List<String>> stringListMap = debianAnalysisServiceImpl
            .querySingleDpkgDependency("112d40ae881e", "mysql-community-server");
        System.out.println("single dpkg dpeendency map size : " + stringListMap.size());
        for (Map.Entry<String, List<String>> entry : stringListMap.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue().toString();
            System.out.println(key + " -> " + val);
        }
    }

    // 测试以单个dpkg来构建拓扑图
    @Test
    public void testDrawSingleDpkgDependency() {
        Map<String, List<String>> stringListMap = debianAnalysisServiceImpl
            .querySingleDpkgDependency("112d40ae881e", "mysql-community-server");
        Boolean res = debianAnalysisServiceImpl.drawDependenciesTopology(stringListMap);
        if (res) {
            System.out.println("构建dpkg依赖拓扑成功");
        } else {
            System.out.println("构建dpkg依赖拓扑失败");
        }
    }

    // 测试查询多条可执行文件路径查询相关依赖
    @Test
    public void testQueryMultipleFileDependencies() {
        List<String> filePaths = new ArrayList<>();
        filePaths.add("/usr/bin/mysql");
        filePaths.add("/usr/sbin/mysqld");
        List<String> needKeepDpkgs = debianAnalysisServiceImpl.queryMultipleFileDependencies("112d40ae881e", filePaths);
        System.out.println("dpkgs size : " + needKeepDpkgs.size());
        for (String str : needKeepDpkgs) {
            System.out.println(str);
        }
    }

    // 测试查询是否存在以app名称安装的dpkg
    @Test
    public void testQueryInstalledDpkgByAPPName() {
        List<String> dpkgs = debianAnalysisServiceImpl.queryInstalledDpkgByAPPName("112d40ae881e", "mysql");
        if (dpkgs == null || dpkgs.size() == 0) return ;
        for (String str : dpkgs) {
            System.out.println(str);
        }
    }

    // 测试列出所有需要保留的依赖项
    @Test
    public void testListNeedKeepDpkgs() {
        List<String> filePaths = new ArrayList<>();
        filePaths.add("/usr/bin/mysql");
        filePaths.add("/usr/sbin/mysqld");
        List<String> mysqlDpkgs = debianAnalysisServiceImpl.listNeedKeepDpkgs("112d40ae881e", filePaths, "mysql");
        System.out.println("need dpkgs size: " + mysqlDpkgs.size());
        for (String str : mysqlDpkgs) {
            System.out.println(str);
        }
    }

    // 测试列出需要删除的依赖项
    @Test
    public void testListNeedDeleteDpkgs() {
        List<String> filePaths = new ArrayList<>();
        filePaths.add("/usr/bin/mysql");
        filePaths.add("/usr/sbin/mysqld");
        List<String> mysqlDpkgs = debianAnalysisServiceImpl.listNeedKeepDpkgs("112d40ae881e", filePaths, "mysql");
        List<String> deleteDpkgs = debianAnalysisServiceImpl.listNeedDeleteDpkgs("112d40ae881e", mysqlDpkgs);
        System.out.println("delete dpkgs size : " + deleteDpkgs.size());
        for (String str : deleteDpkgs) {
            System.out.println(str);
        }
    }

    // 测试删除容器内相关dpkg依赖包
    @Test
    public void testDeleteDpkgDependencies() {
        List<String> filePaths = new ArrayList<>();
        filePaths.add("/usr/bin/mysql");
        filePaths.add("/usr/sbin/mysqld");
        List<String> mysqlDpkgs = debianAnalysisServiceImpl.listNeedKeepDpkgs("112d40ae881e", filePaths, "mysql");
        List<String> needDeleteDpkgs = debianAnalysisServiceImpl.listNeedDeleteDpkgs("112d40ae881e", mysqlDpkgs);
        Boolean res = debianAnalysisServiceImpl.deleteDpkgDependencies("112d40ae881e", needDeleteDpkgs);
        if (res) {
            System.out.println("删除依赖项成功");
        } else {
            System.out.println("删除依赖项失败");
        }

    }





    // 测试使用commit导出为新镜像
    @Test
    public void testCommitToImage() {
        String imageName = "wrf-optimize";
        String tag = "v1";
        Boolean commitRes = debianAnalysisServiceImpl.commitToImage("112d40ae881e", imageName, tag);
        if (commitRes) {
            System.out.println("commit导出镜像成功");
        } else {
            System.out.println("commit导出镜像失败");
        }
    }

    // 测试使用export导出为新镜像
    @Test
    public void testExportToTarImage() {
        String imageName = "mysql-optimize";
        String tag = "v1";
        String path = "/root/docker_images";
        Boolean exportRes = debianAnalysisServiceImpl.exportToTarImage("112d40ae881e", imageName, tag, path);
        if (exportRes) {
            System.out.println("export导出tar镜像成功");
        } else {
            System.out.println("export导出tar镜像失败");
        }
    }

    // 测试使用import导入tar格式文件为新镜像
    @Test
    public void testImportTarToImage() {
        String path = "/root/docker_images";
        String sourTarImageName = "wrf-optimize-export-v1-2023-12-30.tar";
        String destImageName = "wrf-optimize-import";
        String tag = "v1";
        Boolean importRes = debianAnalysisServiceImpl.importTarToImage(path, sourTarImageName, destImageName, tag);
        if (importRes) {
            System.out.println("import导入tar镜像成功");
        } else {
            System.out.println("import导入tar镜像失败");
        }
    }
}
