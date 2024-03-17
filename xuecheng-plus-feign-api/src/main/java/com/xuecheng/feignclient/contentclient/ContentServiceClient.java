package com.xuecheng.feignclient.contentclient;

import com.xuecheng.po.CoursePublish;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(value = "content-api",fallbackFactory = ContentServiceClientFallbackFactory.class)
@RequestMapping("/content")
public interface ContentServiceClient {
    // 查询课程发布
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursePublish(@PathVariable("courseId") Long courseId);
}
