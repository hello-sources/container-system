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
import java.util.ArrayList;
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
        List<String> allRpmLists = analysisServiceImpl.getAllRpmLists("30a9b9bec984");
        for (String str : allRpmLists) {
            System.out.println(str);
        }
        System.out.println(allRpmLists.size());
        return ;
    }

    // 测试获取将rpm包信息保存到文件中
    @Test
    public void testSaveRpmsToFile() {
        List<String> allRpmLists = analysisServiceImpl.getAllRpmLists("30a9b9bec984");
        analysisServiceImpl.writeListToFile(allRpmLists, "30a9b9bec984");
    }

    // 测试获取特定的rpm包信息
    @Test
    public void testAnalysisDependencies() {
        Map<String, String> stringStringMap = analysisServiceImpl.queryDependencies("libgcc", "30a9b9bec984");
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
        List<String> allRpms = analysisServiceImpl.getAllRpmLists("30a9b9bec984");
        Map<String, List<String>> allDeps = analysisServiceImpl.buildOriginDependencies(allRpms, "30a9b9bec984");
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
        List<String> allRpms = analysisServiceImpl.getAllRpmLists("30a9b9bec984");
        Map<String, List<String>> allDeps = analysisServiceImpl.buildOriginDependencies(allRpms, "30a9b9bec984");
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
        List<String> rpms = analysisServiceImpl
            .querySingleFileDependency("30a9b9bec984", "/linpack-xtreme/linpack-xtreme-1.1"
                + ".5-amd64/AuthenticAMD");
        System.out.println("rpm size = " + rpms.size());
        for (int i = 0; i < rpms.size(); i++) {
            System.out.println("rpm library :" + rpms.get(i));
        }
    }

    // 测试多条可执行文件路径查询相关依赖
    @Test
    public void testQueryMultipleFileDependencies() {
        List<String> filePaths = new ArrayList<>();
        filePaths.add("/lib64/libuser.so.1.5.0");
        filePaths.add("/linpack-xtreme/linpack-xtreme-1.1.5-amd64/AuthenticAMD");
        List<String> rpms = analysisServiceImpl.queryMultipleFileDependencies("30a9b9bec984", filePaths);
        System.out.println("rpm size :" + rpms.size());
        for (int i = 0; i < rpms.size(); i++) {
            System.out.println("rpm library : " + rpms.get(i));
        }
    }

    // 测试从一个依赖项出发，获取以该依赖为中心的关系拓扑图
    @Test
    public void testQuerySingleRpmDependency() {
        Map<String, List<String>> stringListMap = analysisServiceImpl
            .querySingleRpmDependency("30a9b9bec984", "coreutils");
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
            .querySingleRpmDependency("30a9b9bec984", "coreutils");
        Boolean drawSucceed = analysisServiceImpl.drawDependenciesTopology(stringListMap);
        if (drawSucceed) {
            System.out.println("----draw single rpm dependency vision topology succeed----");
        } else {
            System.out.println("----draw single rpm dependency vision topology failed----");
        }
    }

    // 获取手动删除过程中必须要保留的rpm依赖
    @Test
    public void testAccessReservationDependencies() {
        List<String> allRpmLists = analysisServiceImpl.getAllRpmLists("30a9b9bec984");
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

    // 测试列出要删除的依赖包
    @Test
    public void testListNeedDeleteRpms() {
        List<String> filePaths = new ArrayList<>();
        filePaths.add("/linpack-xtreme/linpack-xtreme-1.1.5-amd64/AuthenticAMD");
        List<String> needDeleteRpms = analysisServiceImpl.listNeedDeleteRpms("30a9b9bec984", filePaths);
        System.out.println("---need delete rpm number is: " + needDeleteRpms.size() + "----");
        for (String str : needDeleteRpms) {
            System.out.println(str);
        }
    }
}
