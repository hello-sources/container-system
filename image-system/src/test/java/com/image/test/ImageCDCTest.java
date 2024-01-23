package com.image.test;

import com.image.fastcdc4j.external.chunking.Chunk;
import com.image.fastcdc4j.external.chunking.Chunker;
import com.image.fastcdc4j.external.chunking.ChunkerBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * ImageCDCTest
 *
 * @Author litianwei
 * @Date 2024/1/22
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class ImageCDCTest {

    // 测试使用FastCDC项目依赖
    @Test
    public void testFastCDC4J() throws IOException {
        Chunker chunkerBuilder = new ChunkerBuilder().fastCdc().build();
        String buildPath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCFile\\sheldon-lee.mp4";
        String cachePath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCCache";
        Path path = Paths.get(cachePath);
        System.out.println("cache Path : " + path);
        Iterable<Chunk> chunk = chunkerBuilder.chunk(Paths.get(buildPath));
        List<String> fileNames = new ArrayList<>();
        for (Chunk chk : chunk) {
            Path resolve = path.resolve(chk.getHexHash());
            fileNames.add(chk.getHexHash());
            System.out.println("chunk write path : " + resolve);
            Files.write(resolve, chk.getData());
            // if (!Files.exists(resolve) && Files.isWritable(resolve)) {
            //     System.out.println("i = " + i);
            //     i++;
            //     Files.write(resolve, chk.getData());
            // }
        }
    }

    // 使用源码的方式，引入FastCDC进行分块
    @Test
    public void testFastCDC4JSourceCode() throws IOException {
        Chunker chunkerBuilder = new ChunkerBuilder().fastCdc().build();
        String buildPath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCFile\\sheldon-lee.mp4";
        String cachePath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCCache";
        Path path = Paths.get(cachePath);
        System.out.println("cache Path : " + path);

        // 对原目录中的文件进行分块
        Iterable<Chunk> chunk = chunkerBuilder.chunk(Paths.get(buildPath));
        List<String> fileNames = new ArrayList<>();
        for (Chunk chk : chunk) {
            Path resolve = path.resolve(chk.getHexHash());
            fileNames.add(chk.getHexHash());
            System.out.println("chunk write path : " + resolve);
            Files.write(resolve, chk.getData());
        }

        // 根据分块之后的数据进行读取拼接，还原为源文件
        Path targetFile = Paths.get("D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCRebuild\\sheldon-lee"
            + "-rebuild.mp4");
        try {
            // 创建目标文件，如果不存在
            // Files.createFile(targetFile);

            // 打开目标文件，并使用追加模式
            try (SeekableByteChannel targetChannel = Files.newByteChannel(
                targetFile,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE
            )) {
                // 逐个拷贝源文件的内容到目标文件
                for (String fileName : fileNames) {
                    Path sourceFile = Paths.get(cachePath + "\\" + fileName);
                    byte[] fileBytes = Files.readAllBytes(sourceFile);
                    targetChannel.write(ByteBuffer.wrap(fileBytes));
                }
            }

            System.out.println("文件拼接成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
