package com.image.test;

import com.image.imagebench.entity.ContainerCreateTimeEntity;
import com.image.imagebench.mapper.ImageBenchMapper;
import com.image.imagebench.service.impl.ImageBenchImpl;
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

    @Autowired
    private ImageBenchImpl imageBenchImpl;


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

    // 测试获取容器创建时间
    @Test
    public void testGetContainerCreateTime() {
        // String imageName = "mysql-optimize";
        // String tag = "v1";
        String imageName = "mysql";
        String tag = "5.6";
        Long containerCreateTime = imageBenchImpl.getContainerCreateTime(imageName, tag);
        System.out.println("duration time : " + containerCreateTime);
    }

    // 将实际测试数据写入数据库
    @Test
    public void testWriteRealDataToDatabase() {
        ContainerCreateTimeEntity entity = new ContainerCreateTimeEntity();
        entity.setImage_id("a330cd5bb087");
        entity.setImage_tag("v1");
        entity.setImage_name("mysql-optimize");
        entity.setImage_size("269MB");
        entity.setContainer_id("");
        List<BigDecimal> data = new ArrayList<>();
        data.add(new BigDecimal(754));
        data.add(new BigDecimal(789));
        data.add(new BigDecimal(849));
        data.add(new BigDecimal(763));
        data.add(new BigDecimal(753));

        for (BigDecimal big : data) {
            entity.setCreate_time(big);
            List<ContainerCreateTimeEntity> ans = new ArrayList<>();
            ans.add(entity);
            int res = imageBenchMapper.insertResults(ans);
            System.out.println(res);
        }
    }

    // 批量进行测试，将结果写入数据库
    @Test
    public void testBatchBench() {
        String imageName = "wrf";
        String tag = "latest";
        ContainerCreateTimeEntity entity = new ContainerCreateTimeEntity();
        entity.setImage_id("b5d12caa526a");
        entity.setImage_tag(tag);
        entity.setImage_name(imageName);
        entity.setImage_size("3.81GB");
        entity.setContainer_id("");
        for (int i = 0; i < 10; i++) {
            Long containerCreateTime = imageBenchImpl.getContainerCreateTime(imageName, tag);
            entity.setCreate_time(new BigDecimal(containerCreateTime));
            List<ContainerCreateTimeEntity> ans = new ArrayList<>();
            ans.add(entity);
            int res = imageBenchMapper.insertResults(ans);
            System.out.println(res);
        }
    }


}
