package com.image.test;

import com.image.fastcdc4j.external.chunking.Chunk;
import com.image.fastcdc4j.external.chunking.Chunker;
import com.image.fastcdc4j.external.chunking.ChunkerBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        // 测试拼接两次数据，形成重复数据
        List<String> testList = new ArrayList<>();
        testList.addAll(fileNames);
        testList.addAll(fileNames);


        // 根据分块之后的数据进行读取拼接，还原为源文件
        Path targetFile = Paths.get("D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCRebuild\\sheldon-lee"
            + "-rebuild-double.mp4");
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

    // 测试针对高重复率的文件，能否切割成重复的数据块
    @Test
    public void testFastCDC4JDedupChunk() throws IOException {
        Chunker chunkerBuilder = new ChunkerBuilder().fastCdc().build();
        String buildPath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCFile\\article-double.pdf";
        String cachePath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCCache\\article-double-cache";
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

        // 测试拼接两次数据，形成重复数据
        List<String> testList = new ArrayList<>();
        testList.addAll(fileNames);
        testList.addAll(fileNames);


        // 根据分块之后的数据进行读取拼接，还原为源文件
        Path targetFile = Paths.get("D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCRebuild\\article"
            + "-double-rebuild.pdf");
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

    // 交叉对比冗余数据分块的文件，重复率多大
    @Test
    public void testDedupRatio() {
        Set<String> fileSet = new HashSet<>();
        String folderPath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCCache\\article-cache";

        List<String> fileList_double = new ArrayList<>();
        String folderPath_double = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCCache\\article-double"
            + "-cache";

        File folder = new File(folderPath);
        File[] files = folder.listFiles(); // 获取文件夹中的所有文件

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) { // 判断是否为文件
                    fileSet.add(file.getName()); // 获取文件名并添加到列表中
                }
            }
        }
        System.out.println("fileList size : " + fileSet.size());

        File folder_double = new File(folderPath_double);
        File[] files_double = folder_double.listFiles(); // 获取文件夹中的所有文件

        if (files != null) {
            for (File file : files_double) {
                if (file.isFile()) { // 判断是否为文件
                    fileList_double.add(file.getName()); // 获取文件名并添加到列表中
                }
            }
        }
        System.out.println("fileList_double size : " + fileList_double.size());

        int cnt = 0;
        for (String fileName : fileList_double) {
            if (fileSet.contains(fileName)) {
                cnt++;
                System.out.println(fileName);
            }
        }
        System.out.println("cnt size : " + cnt);
    }
}
