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

    @Test
    public void getRpmList() {
        List<String> allRpmLists = analysisServiceImpl.getAllRpmLists("fdce0934ac89");
        for (String str : allRpmLists) {
            System.out.println(str);
        }
        System.out.println(allRpmLists.size());
        return ;
    }

}
