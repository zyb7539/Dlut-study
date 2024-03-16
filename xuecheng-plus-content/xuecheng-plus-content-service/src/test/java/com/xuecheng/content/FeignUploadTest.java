package com.xuecheng.content;

import com.xuecheng.config.MultipartSupportConfig;

import com.xuecheng.feignclient.meidaclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 测试远程调用媒资服务
 * */
@SpringBootTest
public class FeignUploadTest {
    @Autowired
    MediaServiceClient mediaServiceClient;

    @Test
    public void test() throws IOException {
        // 将file类型转为Multipart
        File file = new File("F:\\120.html");
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);

        String upload = mediaServiceClient.upload(multipartFile, "course/120.html");
        if(upload == null){
            System.out.println("发生了降级");
        }
    }
}
