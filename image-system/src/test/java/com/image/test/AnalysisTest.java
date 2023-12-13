package com.image.test;

import com.image.analysis.service.impl.AnalysisServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        // containerID fdce0934ac89, 暂时使用Linpack—xtreme的容器
        List<String> allRpmLists = analysisServiceImpl.getAllRpmLists("fdce0934ac89");
        for (String str : allRpmLists) {
            System.out.println(str);
        }
        System.out.println(allRpmLists.size());
        return ;
    }

    // 测试获取将rpm包信息保存到文件中
    @Test
    public void testSaveRpmsToFile() {
        List<String> allRpmLists = analysisServiceImpl.getAllRpmLists("fdce0934ac89");
        analysisServiceImpl.writeListToFile(allRpmLists, "fdce0934ac89");
    }

    // 测试获取特定的rpm包信息
    @Test
    public void testAnalysisDependencies() {
        Map<String, String> stringStringMap = analysisServiceImpl.queryDependencies("libgcc", "fdce0934ac89");
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
        List<String> allRpms = analysisServiceImpl.getAllRpmLists("fdce0934ac89");
        Map<String, List<String>> allDeps = analysisServiceImpl.buildOriginDependencies(allRpms, "fdce0934ac89");
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
        List<String> allRpms = analysisServiceImpl.getAllRpmLists("fdce0934ac89");
        Map<String, List<String>> allDeps = analysisServiceImpl.buildOriginDependencies(allRpms, "fdce0934ac89");
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
        List<String> allRpmLists = analysisServiceImpl.getAllRpmLists("fdce0934ac89");
        analysisServiceImpl.writeSimpleListToFile(allRpmLists, "fdce0934ac89");
    }

    // 测试单条可执行文件路径查询相关依赖
    @Test
    public void testQuerySingleFileDependency() {
        List<String> rpms = analysisServiceImpl
            .querySingleFileDependency("fdce0934ac89", "/linpack-xtreme/linpack-xtreme-1.1"
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
        List<String> rpms = analysisServiceImpl.queryMultipleFileDependencies("fdce0934ac89", filePaths);
        System.out.println("rpm size :" + rpms.size());
        for (int i = 0; i < rpms.size(); i++) {
            System.out.println("rpm library : " + rpms.get(i));
        }
    }
}
