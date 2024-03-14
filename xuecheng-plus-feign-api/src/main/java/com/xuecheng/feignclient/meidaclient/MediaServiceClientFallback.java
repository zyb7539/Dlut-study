package com.xuecheng.feignclient.meidaclient;

import com.xuecheng.feignclient.meidaclient.MediaServiceClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MediaServiceClientFallback implements MediaServiceClient {
    @Override
    public String upload(MultipartFile fileData, String objectName) throws IOException {


        return null;
    }
}
