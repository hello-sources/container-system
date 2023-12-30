package com.image.test;

import com.image.analysis.service.impl.AnalysisServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AnalysisTests
 *
 * @Author litianwei
 * @Date 2023/12/7
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class AnalysisTest {

    @Autowired
    private AnalysisServiceImpl analysisServiceImpl;

    // 测试获取容器内rpm包信息
    @Test
    public void getRpmList() {
        // containerID 30a9b9bec984, 暂时使用Linpack—xtreme的容器
        // containerID f31a185d234f WRF镜像创建的容器
        List<String> allRpmLists = analysisServiceImpl.getAllRpmLists("f31a185d234f");
        for (String str : allRpmLists) {
            System.out.println(str);
        }
        System.out.println(allRpmLists.size());
        return ;
    }

    // 测试获取将rpm包信息保存到文件中
    @Test
    public void testSaveRpmsToFile() {
        List<String> allRpmLists = analysisServiceImpl.getAllRpmLists("f31a185d234f");
        analysisServiceImpl.writeListToFile(allRpmLists, "f31a185d234f");
    }

    // 测试获取特定的rpm包信息
    @Test
    public void testAnalysisDependencies() {
        Map<String, String> stringStringMap = analysisServiceImpl.queryDependencies("libgcc-4.8.5-44.el7.x86_64", "f31a185d234f");
        System.out.println("Total dependencies is " + stringStringMap.size());
        for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("key: " + key + ", value: " + value);
        }
        return ;
    }

    // 测试构建所有的rpm包依赖信息
    @Test
    public void testAllRpmDependencies() {
        List<String> allRpms = analysisServiceImpl.getAllRpmLists("f31a185d234f");
        // List<String> allRpms = new ArrayList<>();
        // allRpms.add("libgcc-4.8.5-44.el7.x86_64");
        Map<String, List<String>> allDeps = analysisServiceImpl.buildOriginDependencies(allRpms, "f31a185d234f");
        System.out.println("--------" + allDeps.size() + "--------");
        for (Map.Entry<String, List<String>> entry : allDeps.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            System.out.println(key + " -> " + value);
        }
    }

    // 测试构建所有rpm包信息dot图
    @Test
    public void testDrawDependenciesTopology() {
        List<String> allRpms = analysisServiceImpl.getAllRpmLists("f31a185d234f");
        Map<String, List<String>> allDeps = analysisServiceImpl.buildOriginDependencies(allRpms, "f31a185d234f");
        Boolean draw = analysisServiceImpl.drawDependenciesTopology(allDeps);
        if (draw) {
            System.out.println("----draw dependencies succeed----");
        } else {
            System.out.println("----draw dependencies failed----");
        }
    }

    // 测试构建简单格式的rpm到文件中
    @Test
    public void testWriteSimpleListToFile() {
        List<String> allRpmLists = analysisServiceImpl.getAllRpmLists("30a9b9bec984");
        analysisServiceImpl.writeSimpleListToFile(allRpmLists, "30a9b9bec984");
    }

    // 测试单条可执行文件路径查询相关依赖
    @Test
    public void testQuerySingleFileDependency() {
        // List<String> rpms = analysisServiceImpl
        //     .querySingleFileDependency("30a9b9bec984", "/linpack-xtreme/linpack-xtreme-1.1"
        //         + ".5-amd64/AuthenticAMD");

        List<String> rpms = analysisServiceImpl.querySingleFileDependency("f31a185d234f", "/comsoftware/wrf/WPS-4"
            + ".3/geogrid.exe");

        System.out.println("rpm size = " + rpms.size());
        for (int i = 0; i < rpms.size(); i++) {
            System.out.println("rpm library :" + rpms.get(i));
        }
    }

    // 测试多条可执行文件路径查询相关依赖
    @Test
    public void testQueryMultipleFileDependencies() {
        List<String> filePaths = new ArrayList<>();

        filePaths.add("/comsoftware/wrf/WPS-4.3/geogrid.exe");
        filePaths.add("/comsoftware/wrf/WPS-4.3/ungrib.exe");
        filePaths.add("/comsoftware/wrf/WPS-4.3/metgrid.exe");
        filePaths.add("/comsoftware/wrf/WRF-4.3/run/real.exe");
        filePaths.add("/comsoftware/wrf/WRF-4.3/run/wrf.exe");
        filePaths.add("/usr/local/bin/mpirun");
        filePaths.add("/usr/bin/ln");


        List<String> rpms = analysisServiceImpl.queryMultipleFileDependencies("f31a185d234f", filePaths);
        System.out.println("rpm size :" + rpms.size());
        for (int i = 0; i < rpms.size(); i++) {
            System.out.println("rpm library : " + rpms.get(i));
        }
    }

    // 测试从一个依赖项出发，获取以该依赖为中心的关系拓扑图
    @Test
    public void testQuerySingleRpmDependency() {
        Map<String, List<String>> stringListMap = analysisServiceImpl
            .querySingleRpmDependency("f31a185d234f", "nspr-4.32.0-1.el7_9.x86_64");
        System.out.println("----relative dependencies size " + stringListMap.size() + "----");
        for (Map.Entry<String, List<String>> entry : stringListMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            System.out.println("key : " + key + " value : " + value);
        }
    }

    // 测试以单个依赖为中心，画出依赖拓扑图
    @Test
    public void testDrawSingleVisionTopology() {
        Map<String, List<String>> stringListMap = analysisServiceImpl
            .querySingleRpmDependency("f31a185d234f", "glibc-2.17-325.el7_9.x86_64");
        Boolean drawSucceed = analysisServiceImpl.drawDependenciesTopology(stringListMap);
        if (drawSucceed) {
            System.out.println("----draw single rpm dependency vision topology succeed----");
        } else {
            System.out.println("----draw single rpm dependency vision topology failed----");
        }
    }

    // 获取手动删除过程中可能要删除的rpm依赖
    @Test
    public void testAccessReservationDependencies() {
        List<String> allRpmLists = analysisServiceImpl.getAllRpmLists("f31a185d234f");
        Set<String> set = new HashSet<>();
        for (String rpm : allRpmLists) {
            set.add(rpm);
        }

        // 根据手动裁剪的记录获取需要保留的包名
        String filePath = "D:\\Workspace\\container-system\\image-system\\src\\conf\\keepReservationDependencies.conf";

        List<String> handProcess = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // 逐行读取文件内容
            String line = new String();
            while ((line = br.readLine()) != null) {
                // 将每行添加到List中
                handProcess.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String str : handProcess) {
            if (set.contains(str)) set.remove(str);
        }
        System.out.println("----the rpm number is: " + set.size() + "----");
        for (String setElement : set) {
            System.out.println(setElement);
        }
    }

    // 测试列出所有必须要删除的依赖包
    @Test
    public void testListNeedDeleteRpmsPreCode() {
        List<String> filePaths = new ArrayList<>();
        // filePaths.add("/linpack-xtreme/linpack-xtreme-1.1.5-amd64/AuthenticAMD");

        filePaths.add("/comsoftware/wrf/WPS-4.3/geogrid.exe");
        filePaths.add("/comsoftware/wrf/WPS-4.3/ungrib.exe");
        filePaths.add("/comsoftware/wrf/WPS-4.3/metgrid.exe");
        filePaths.add("/comsoftware/wrf/WRF-4.3/run/real.exe");
        filePaths.add("/comsoftware/wrf/WRF-4.3/run/wrf.exe");
        filePaths.add("/usr/local/bin/mpirun");
        filePaths.add("/usr/bin/ln");


        List<String> directDependencies = analysisServiceImpl.queryMultipleFileDependencies("f31a185d234f", filePaths);
        System.out.println("needList size = " + directDependencies.size());
        for (String str : directDependencies) {
            System.out.println(str);
        }

        Map<String, List<String>> rpmListMap = new HashMap<>();
        for (String direct : directDependencies) {
            Map<String, List<String>> stringListMap = analysisServiceImpl.querySingleRpmDependency("f31a185d234f", direct);
            for (Map.Entry<String, List<String>> entry : stringListMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                System.out.println("key : " + key + " value : " + value);
            }
            for (Map.Entry<String, List<String>> entry : stringListMap.entrySet()) {
                if (rpmListMap.containsKey(entry.getKey())) {
                    rpmListMap.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    rpmListMap.put(entry.getKey(), entry.getValue());
                }
            }
        }

        System.out.println("map size : " + rpmListMap.size());
        for (Map.Entry<String, List<String>> entry : rpmListMap.entrySet()) {
            System.out.println("key = " + entry.getKey());
            System.out.println("value = " + entry.getValue());
        }

        List<String> needDeleteRpms = analysisServiceImpl.listNeedDeleteRpms("f31a185d234f", filePaths);
        System.out.println("---need delete rpm number is: " + needDeleteRpms.size() + "----");
        for (String str : needDeleteRpms) {
            System.out.println(str);
        }
    }

    // 测试列出所有待删除的rpm包
    @Test
    public void testListNeedDeleteRpms() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        System.out.println("开始进行测试的时间：" + formattedDateTime);
        List<String> filePaths = new ArrayList<>();
        // filePaths.add("/linpack-xtreme/linpack-xtreme-1.1.5-amd64/AuthenticAMD");

        filePaths.add("/comsoftware/wrf/WPS-4.3/geogrid.exe");
        filePaths.add("/comsoftware/wrf/WPS-4.3/ungrib.exe");
        filePaths.add("/comsoftware/wrf/WPS-4.3/metgrid.exe");
        filePaths.add("/comsoftware/wrf/WRF-4.3/run/real.exe");
        filePaths.add("/comsoftware/wrf/WRF-4.3/run/wrf.exe");
        filePaths.add("/usr/local/bin/mpirun");
        filePaths.add("/usr/bin/ln");

        List<String> needDeleteRpms = analysisServiceImpl.listNeedDeleteRpms("f31a185d234f", filePaths);
        System.out.println("---need delete rpm number is: " + needDeleteRpms.size() + "----");
        for (String str : needDeleteRpms) {
            System.out.println(str);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("结束测试的时间：" + LocalDateTime.now().format(formatter));
        System.out.println("共计测试时间, 从 " + now.format(formatter) + " 持续到 ： " + LocalDateTime.now().format(formatter));
    }

    // 测试列出所有要保留的包
    @Test
    public void testListKeepRpms() {
        List<String> filePaths = new ArrayList<>();
        filePaths.add("/linpack-xtreme/linpack-xtreme-1.1.5-amd64/AuthenticAMD");
        List<String> keepRpms = analysisServiceImpl.listKeepRpms("30a9b9bec984", filePaths);
        System.out.println("---need delete rpm number is: " + keepRpms.size() + "----");
        for (String str : keepRpms) {
            System.out.println(str);
        }
    }

    // 测试列出自定义需要保留的包
    @Test
    public void testKeepRpmDependencies() {
        List<String> filePaths = new ArrayList<>();
        filePaths.add("/linpack-xtreme/linpack-xtreme-1.1.5-amd64/AuthenticAMD");
        List<String> rpmNames = new ArrayList<>();
        rpmNames.add("vim-minimal-7.4.629-7.el7.x86_64");
        rpmNames.add("centos-release-7-9.2009.0.el7.centos.x86_64");
        List<String> keepRpmDependencies = analysisServiceImpl.keepRpmDependencies("30a9b9bec984", rpmNames, filePaths);
        System.out.println("----keep dependencies size is : " + keepRpmDependencies.size() + "----");
        for (String str : keepRpmDependencies) {
            System.out.println(str);
        }
    }

    // 测试删除rpm包以及导出为镜像
    @Test
    public void testDeleteAndCommitToImage() {
        List<String> filePaths = new ArrayList<>();
        // filePaths.add("/linpack-xtreme/linpack-xtreme-1.1.5-amd64/AuthenticAMD");

        filePaths.add("/comsoftware/wrf/WPS-4.3/geogrid.exe");
        filePaths.add("/comsoftware/wrf/WPS-4.3/ungrib.exe");
        filePaths.add("/comsoftware/wrf/WPS-4.3/metgrid.exe");
        filePaths.add("/comsoftware/wrf/WRF-4.3/run/real.exe");
        filePaths.add("/comsoftware/wrf/WRF-4.3/run/wrf.exe");
        filePaths.add("/usr/local/bin/mpirun");
        filePaths.add("/usr/bin/ln");


        List<String> needDeleteRpms = analysisServiceImpl.listNeedDeleteRpms("f31a185d234f", filePaths);

        System.out.println("---need delete rpm number is: " + needDeleteRpms.size() + "----");
        for (String str : needDeleteRpms) {
            System.out.println(str);
        }

        String imageName = "wrf-optimize";
        String tag = "v1";
        Boolean deleteRes = analysisServiceImpl.deleteRpmDependencies("f31a185d234f", needDeleteRpms);
        Boolean commitRes = analysisServiceImpl.commitToImage("f31a185d234f", imageName, tag);
        if (deleteRes && commitRes) {
            System.out.println("优化镜像成功");
        } else {
            System.out.println("优化镜像失败");
        }
    }

    // 测试删除rpm包
    @Test
    public void testDeleteRpmDependencies() {
        List<String> filePaths = new ArrayList<>();
        // filePaths.add("/linpack-xtreme/linpack-xtreme-1.1.5-amd64/AuthenticAMD");

        filePaths.add("/comsoftware/wrf/WPS-4.3/geogrid.exe");
        filePaths.add("/comsoftware/wrf/WPS-4.3/ungrib.exe");
        filePaths.add("/comsoftware/wrf/WPS-4.3/metgrid.exe");
        filePaths.add("/comsoftware/wrf/WRF-4.3/run/real.exe");
        filePaths.add("/comsoftware/wrf/WRF-4.3/run/wrf.exe");
        filePaths.add("/usr/local/bin/mpirun");
        filePaths.add("/usr/bin/ln");

        List<String> needDeleteRpms = analysisServiceImpl.listNeedDeleteRpms("f31a185d234f", filePaths);

        System.out.println("---need delete rpm number is: " + needDeleteRpms.size() + "----");
        for (String str : needDeleteRpms) {
            System.out.println(str);
        }

        Boolean deleteRes = analysisServiceImpl.deleteRpmDependencies("f31a185d234f", needDeleteRpms);
        if (deleteRes) {
            System.out.println("删除依赖成功");
        } else {
            System.out.println("删除依赖失败");
        }
    }



    // 测试使用commit导出为新镜像
    @Test
    public void testCommitToImage() {
        String imageName = "wrf-optimize";
        String tag = "v1";
        Boolean commitRes = analysisServiceImpl.commitToImage("f31a185d234f", imageName, tag);
        if (commitRes) {
            System.out.println("commit导出镜像成功");
        } else {
            System.out.println("commit导出镜像失败");
        }
    }

    // 测试使用export导出为新镜像
    @Test
    public void testExportToTarImage() {
        String imageName = "wrf-optimize";
        String tag = "v1";
        String path = "/root/docker_images";
        Boolean exportRes = analysisServiceImpl.exportToTarImage("f31a185d234f", imageName, tag, path);
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
        Boolean importRes = analysisServiceImpl.importTarToImage(path, sourTarImageName, destImageName, tag);
        if (importRes) {
            System.out.println("import导入tar镜像成功");
        } else {
            System.out.println("import导入tar镜像失败");
        }
    }
}
