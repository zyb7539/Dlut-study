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
        //TODO minio的服务器文件与本地文件的md5检验
        //上传文件
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("/test01/0001.png").build();
            FilterInputStream re = minioClient.getObject(getObjectArgs);

            //下载到本地
            FileOutputStream fileOutputStream = new FileOutputStream(new File("F:\\1.png"));
            IOUtils.copy(re,fileOutputStream);

            String s1 = DigestUtils.md5Hex(re);
            String s2 = DigestUtils.md5Hex(Files.newInputStream(new File("F:\\1.png").toPath()));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
