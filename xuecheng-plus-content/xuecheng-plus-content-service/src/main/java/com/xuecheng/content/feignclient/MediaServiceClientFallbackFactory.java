package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@Slf4j
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {

        return new MediaServiceClient() {
            @Override
            public String upload(MultipartFile fileData, String objectName) throws IOException {
                log.debug("远程调用上传文件接口发送熔断：{}",throwable.toString(),throwable);
                return null;
            }
        };
    }

}
