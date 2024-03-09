package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();
    @Test
    public void testUpload(){
        //上传文件
        try {
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
            String mimeType = extensionMatch.getMimeType();
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("testbucket")
                            .filename("F:\\Knowledge\\netty\\img\\0001.png")
                            .object("/test01/0001.png") //在桶下放在子目录
                            .contentType(mimeType)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    @Test
    public void delete(){
        //上传文件
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket("testbucket").object("0001.png").build();
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    @Test
    public void get(){
        //上传文件
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("/test01/0001.png").build();
            InputStream re = minioClient.getObject(getObjectArgs);

            // 下载到本地
            FileOutputStream fileOutputStream = new FileOutputStream(new File("F:\\1.png"));
            IOUtils.copy(re,fileOutputStream);

            String s1 = DigestUtils.md5Hex(re);
            String s2 = DigestUtils.md5Hex(Files.newInputStream(new File("F:\\1.png").toPath()));
            System.out.println(s1);
            System.out.println(s2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    //分块文件上传minio
    @Test
    public void  uploadChunk() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        for (int i = 0; i < 5; i++) {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .filename("F:\\chunk\\" + i)
                    .object("chunk/" + i) //在桶下放在子目录
                    .build();
            //上传文件
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传分块" + i + "成功");
        }

    }
    //调用minio接口合并分块
    @Test
    public void  testMerge() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // List<ComposeSource> source = new ArrayList<>();
        // for (int i = 0; i < 5; i++) {
        //     ComposeSource build = ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build();
        //     source.add(build);
        //
        // }
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(5)
                .map(i -> ComposeSource.builder()
                        .bucket("testbucket")
                        .object("chunk/".concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());

        ComposeObjectArgs testbucket = ComposeObjectArgs.builder().bucket("testbucket")
                .sources(sources)
                .object("merge.mp4").build();
        minioClient.composeObject(testbucket);
    }
    //删除分块
}

//TODO minio文件排重 通过md5前两位作目录，而不是用年月日