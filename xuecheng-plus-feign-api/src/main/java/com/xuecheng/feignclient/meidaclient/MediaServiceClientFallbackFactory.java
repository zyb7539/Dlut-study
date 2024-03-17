package com.xuecheng.feignclient.meidaclient;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.feignclient.meidaclient.MediaServiceClient;
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

            @Override
            public RestResponse<String> getPlayUrlByMediaId(String mediaId) {
                log.debug("远程调用媒资查询出错发生熔断：{}",throwable.toString(),throwable);
                return null;
            }
        };
    }

}
