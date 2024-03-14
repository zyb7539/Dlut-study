package com.xuecheng.feignclient.meidaclient;


import com.xuecheng.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 远程调用媒资的接口
 * */
// 使用fallback实现降级处理，没法得到异常
// 使用fallbackFactory实现降级处理，拿到熔断的异常信息
@FeignClient(value = "media-api",configuration = {MultipartSupportConfig.class},fallbackFactory = MediaServiceClientFallbackFactory.class)
public interface MediaServiceClient {
    @RequestMapping(value = "/media/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@RequestPart("filedata") MultipartFile fileData,
                                      @RequestParam(value= "objectName",required=false) String objectName) throws IOException;

}
