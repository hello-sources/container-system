package com.image.test;

import com.image.fastcdc4j.external.chunking.Chunk;
import com.image.fastcdc4j.external.chunking.Chunker;
import com.image.fastcdc4j.external.chunking.ChunkerBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ImageCDCTest
 *
 * @Author litianwei
 * @Date 2024/1/22
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class ImageCDCTest {

    // TODO 解决引入FastCDC的各种问题
    // 测试使用FastCDC项目依赖
    @Test
    public void testFastCDC4J() throws IOException {
        // Chunker chunkerBuilder = new ChunkerBuilder().fastCdc().build();
        // String buildPath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCFile\\sheldon-lee.mp4";
        // String cachePath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCCache";
        // Path path = Paths.get(cachePath);
        // Iterable<Chunk> chunk = chunkerBuilder.chunk(Paths.get(buildPath));
        // for (Chunk chk : chunk) {
        //     Path resolve = path.resolve(chk.getHexHash());
        //     if (!Files.exists(resolve)) {
        //         Files.write(path, chk.getData());
        //     }
        // }
    }
}
