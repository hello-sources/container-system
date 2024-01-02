package com.image.test;

import com.image.analysis.service.impl.DebianAnalysisServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
        List<String> allDpkgLists = debianAnalysisServiceImpl.getAllDpkgLists("3023e8383575");
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
        List<String> allDpkgLists = debianAnalysisServiceImpl.getAllDpkgLists("3023e8383575");
        Boolean res = debianAnalysisServiceImpl.writeListToFile(allDpkgLists, "3023e8383575");
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
            .queryDependencies("mysql-community-server", "3023e8383575");

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
            .buildOriginDependencies_absent("3023e8383575");
        System.out.println("map size : " + stringListMap.size());
        for (Map.Entry<String, List<String>> entry : stringListMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            System.out.println("key : " + key + ", value : " + value);
        }
        return ;
    }

}
