package com.xuecheng.feignclient.contentclient;

import com.xuecheng.po.CoursePublish;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ContentServiceClientFallbackFactory implements FallbackFactory<ContentServiceClient> {
    @Override
    public ContentServiceClient create(Throwable throwable) {
        return new ContentServiceClient() {
            @Override
            public CoursePublish getCoursePublish(Long courseId) {
                log.debug("调用课程id查询发布的课程异常:{}", throwable.getMessage());
                return null;
            }
        };
    }
}
