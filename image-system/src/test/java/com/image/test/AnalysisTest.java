package com.image.test;

import com.image.analysis.service.impl.AnalysisServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

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

}