package com.image.test;

import com.alibaba.fastjson.JSONObject;
import com.image.dedup.entity.BloomFilterEntity;
import com.image.dedup.service.impl.DedupServiceImpl;
import com.image.fastcdc4j.external.chunking.Chunk;
import com.image.fastcdc4j.external.chunking.Chunker;
import com.image.fastcdc4j.external.chunking.ChunkerBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBloomFilter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
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

    @Resource
    private DedupServiceImpl dedupServiceImpl;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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

    // 对tar文件进行分块
    @Test
    public void testChunkTarFile() throws IOException {
        Chunker chunkerBuilder = new ChunkerBuilder().fastCdc().build();
        // Chunker chunkerBuilder = new ChunkerBuilder().fsc().build(); 使用FSC/FSP算法进行切分，默认大小8KB
        String buildPath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCFile\\redis-optimize-export-v1-2024-01-04.tar";
        String cachePath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCCache\\redis-optimize-export-v1-2024-01-04";
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
    }

    // 对tar文件进行分块，存放元数据信息到Redis中
    @Test
    public void testChunkAndSaveMetaInfo() throws IOException {
        Chunker chunkerBuilder = new ChunkerBuilder().fastCdc().build();
        String buildPath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCFile\\redis-optimize-export-v1-2024-01-04.tar";
        String cachePath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCCache\\redis-optimize-export"
            + "-v1-2024-04-03";
        Path path = Paths.get(cachePath);
        System.out.println("cache Path : " + path);

        String bucketName = "redis-meta-info";
        Instant before = Instant.now();

        // 对原目录中的文件进行分块
        Iterable<Chunk> chunk = chunkerBuilder.chunk(Paths.get(buildPath));
        List<String> fileNames = new ArrayList<>();
        for (Chunk chk : chunk) {
            String hexHash = chk.getHexHash();
            chk.getLength();
            String jsonString = "{\"key\": \"" + hexHash + "\"}";
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            dedupServiceImpl.addMetaInfoToBucket(bucketName, jsonObject);
            Path resolve = path.resolve(chk.getHexHash());
            fileNames.add(chk.getHexHash());
            System.out.println("chunk write path : " + resolve);
            Files.write(resolve, chk.getData());
        }

        Instant after = Instant.now();
        long durationSeconds = after.getEpochSecond() - before.getEpochSecond();
        System.out.println("持续时间（秒）: " + durationSeconds);
    }

    // 对tar文件进行分块，存放元数据信息到文件中
    @Test
    public void testChunkAndSaveToFile() throws IOException {
        Chunker chunkerBuilder = new ChunkerBuilder().fastCdc().build();
        String buildPath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCFile\\redis-optimize-export-v1-2024-01-04.tar";
        String cachePath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCCache\\redis-optimize-export"
            + "-v2-2024-04-03";
        Path path = Paths.get(cachePath);
        System.out.println("cache Path : " + path);

        String fileName = "redis-meta-info.txt";
        String dirPath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\MetaInfo";
        File file = new File(dirPath, fileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        Instant before = Instant.now();

        // 对原目录中的文件进行分块
        Iterable<Chunk> chunk = chunkerBuilder.chunk(Paths.get(buildPath));
        List<String> fileNames = new ArrayList<>();
        for (Chunk chk : chunk) {
            String hexHash = chk.getHexHash();
            writer.write(hexHash + "\n"); // 写入数据到文件
            Path resolve = path.resolve(hexHash);
            fileNames.add(chk.getHexHash());
            System.out.println("chunk write path : " + resolve);
            Files.write(resolve, chk.getData());
        }

        Instant after = Instant.now();
        long durationSeconds = after.getEpochSecond() - before.getEpochSecond();
        System.out.println("持续时间（秒）: " + durationSeconds);
    }

    // 将数据块元信息写入元信息
    @Test
    public void testWriteMetaInfoToBucket() {
        Chunker chunkerBuilder = new ChunkerBuilder().fastCdc().build();
        String buildPath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCFile\\redis-optimize-export-v1-2024-01-04.tar";

        String bucketName = "image-chunk-meta-info";
        Instant before = Instant.now();

        // 对原目录中的文件进行分块
        Iterable<Chunk> chunk = chunkerBuilder.chunk(Paths.get(buildPath));
        for (Chunk chk : chunk) {
            String hexHash = chk.getHexHash();
            String blockSize = String.format("%.2f", chk.getLength() / 1024.0);
            long offset = chk.getOffset();
            String loc = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCCache\\redis-optimize-export"
                + "-v2-2024-04-03";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", hexHash);
            jsonObject.put("blockSize", blockSize + "KB");
            jsonObject.put("offset", offset);
            jsonObject.put("loc", loc);
            dedupServiceImpl.addMetaInfoToBucket(bucketName, jsonObject);
            System.out.println(hexHash);
        }

        Instant after = Instant.now();
        long durationSeconds = after.getEpochSecond() - before.getEpochSecond();
        System.out.println("持续时间（秒）: " + durationSeconds);
    }

    // 把指纹信息录入布隆过滤器
    @Test
    public void testWriteFingerPrintIntoBloomFilter() {
        Chunker chunkerBuilder = new ChunkerBuilder().fastCdc().build();
        String buildPath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCFile\\redis-optimize-export-v1-2024-01-04.tar";

        BloomFilterEntity entity = new BloomFilterEntity();
        entity.setBloomFilterName("image-bloom-filter1");
        entity.setExpectedInsertions(100000L);
        entity.setFpp(0.000001);
        RBloomFilter<Object> bloomFilter = dedupServiceImpl.createBloomFilter(entity);

        Instant before = Instant.now();

        // 对原目录中的文件进行分块
        Iterable<Chunk> chunk = chunkerBuilder.chunk(Paths.get(buildPath));
        for (Chunk chk : chunk) {
            String hexHash = chk.getHexHash();
            bloomFilter.add(hexHash);
            System.out.println(hexHash);
        }

        Instant after = Instant.now();
        long durationSeconds = after.getEpochSecond() - before.getEpochSecond();
        System.out.println("持续时间（秒）: " + durationSeconds);
    }

    // 测试从布隆过滤器读取指纹信息判断是否存在
    @Test
    public void testReadFingerPrint() {
        String dirPath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCCache\\redis-optimize-export-v2-2024-04-03";
        String bloomFilterName = "image-bloom-filter1";
        File directory = new File(dirPath);
        int cntTrue = 0;
        int cntFalse = 0;

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String name = file.getName();
                        System.out.println(name);
                        Boolean aBoolean = dedupServiceImpl.bloomFilterFindFp(name, bloomFilterName);
                        if (aBoolean) cntTrue++;
                        else cntFalse++;
                    }
                }
            }
        } else {
            System.out.println("Directory does not exist or is not a directory.");
        }

        System.out.println("cnt True : " + cntTrue);
        System.out.println("cnt False : " + cntFalse);
    }

    // 测试从哈希桶中取出元数据信息，找到地址，拼接成完整的镜像
    @Test
    public void testCombineMetaInfoToImage() throws IOException {
        String redisMetaBucket = "redis-meta-info";
        String bloomFilterName = "image-bloom-filter1";
        String bucketName = "image-chunk-meta-info";
        Path targetFile = Paths.get("D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCRebuild\\redis"
            + "-optimize"
            + "-export.tar");

        int cntTrue = 0;
        int cntFalse = 0;

        List<String> range = stringRedisTemplate.opsForList().range(redisMetaBucket, 0, -1);
        List<String> metaInfo = stringRedisTemplate.opsForList().range(bucketName, 0, -1);

        SeekableByteChannel targetChannel = Files.newByteChannel(
            targetFile,
            StandardOpenOption.WRITE,
            StandardOpenOption.APPEND,
            StandardOpenOption.CREATE
        );
        System.out.println("元数据信息总数： " + range.size());
        int cnt = 0;
        for (String str : range) {
            JSONObject jsonObject = JSONObject.parseObject(str);
            String key = jsonObject.get("key").toString();
            System.out.println(key);
            Boolean aBoolean = dedupServiceImpl.bloomFilterFindFp(key, bloomFilterName);
            if (aBoolean) cntTrue++;
            else cntFalse++;
            for (String json : metaInfo) {
                JSONObject meta = JSONObject.parseObject(json);
                if (key.equals(meta.getString("key"))) {
                    String realLoc = meta.getString("loc") + "\\" + key;
                    System.out.println(realLoc);
                    cnt++;
                    // 拼接逻辑
                    Path sourceFile = Paths.get(realLoc);
                    byte[] fileBytes = Files.readAllBytes(sourceFile);
                    targetChannel.write(ByteBuffer.wrap(fileBytes));
                    break;
                }
            }
        }
        System.out.println("元数据信息总数： " + range.size());
        System.out.println("查到的路径总数：" + cnt);
        System.out.println("文件拼接成功！");
        System.out.println("cnt True : " + cntTrue);
        System.out.println("cnt False : " + cntFalse);
    }

    // 尝试使用切割过程产生的数据，进行拼接
    @Test
    public void testCombineFile() throws IOException {
        // Chunker chunkerBuilder = new ChunkerBuilder().fastCdc().build();
        Chunker chunkerBuilder = new ChunkerBuilder().fastCdc_Re().build();
        String buildPath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCFile\\redis-optimize-export-v1-2024-01-04.tar";
        String cachePath = "D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCCache\\redis-optimize-export"
            + "-v3-2024-04-03";
        Path path = Paths.get(cachePath);
        System.out.println("cache Path : " + path);

        // 对原目录中的文件进行分块
        Iterable<Chunk> chunk = chunkerBuilder.chunk(Paths.get(buildPath));
        List<String> fileNames = new ArrayList<>();
        for (Chunk chk : chunk) {
            String hexHash = chk.getHexHash();
            Path resolve = path.resolve(hexHash);
            fileNames.add(chk.getHexHash());
            System.out.println("chunk write path : " + resolve);
            Files.write(resolve, chk.getData());
        }

        System.out.println(fileNames);
        System.out.println("总共文件大小： " + fileNames.size());

        Instant before = Instant.now();

        // 根据分块之后的数据进行读取拼接，还原为源文件
        Path targetFile = Paths.get("D:\\Workspace\\container-system\\image-system\\src\\CDC\\CDCRebuild\\redis"
            + "-optimize-export.tar");
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

        Instant after = Instant.now();
        long durationSeconds = after.getEpochSecond() - before.getEpochSecond();
        System.out.println("持续时间（秒）: " + durationSeconds);
    }
}
