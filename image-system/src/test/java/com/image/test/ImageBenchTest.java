package com.image.test;

import com.image.imagebench.entity.ContainerCreateTimeEntity;
import com.image.imagebench.mapper.ImageBenchMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ImageBenchTest
 *
 * @Author litianwei
 * @Date 2024/1/5
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class ImageBenchTest {

    @Autowired
    private ImageBenchMapper imageBenchMapper;


    // 测试使用Mybatis写入数据到数据库
    @Test
    public void testWriteIntoDatabase() {
        ContainerCreateTimeEntity entity = new ContainerCreateTimeEntity();
        entity.setImage_id("dd3b2a5dcb48");
        entity.setImage_tag("5.6");
        entity.setImage_name("mysql");
        entity.setImage_size("303MB");
        entity.setContainer_id("1e0cf052bf33");
        entity.setCreate_time(new BigDecimal(123.456));
        List<ContainerCreateTimeEntity> ans = new ArrayList<>();
        ans.add(entity);
        int res = imageBenchMapper.insertResults(ans);
        System.out.println(res);
    }


}
