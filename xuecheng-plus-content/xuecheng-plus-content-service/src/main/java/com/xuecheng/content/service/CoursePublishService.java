package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 课程发布接口
 * */
public interface CoursePublishService {
    /**
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);
    /**
     * 提交审核
     * @param courseId
     * @param companyId
     * */
    public void commitAudit(Long companyId, Long courseId);

    /**
     * @description 课程发布接口
     * @param companyId 机构id
     * @param courseId 课程id
     * @return void
     */
    public void publish(Long companyId,Long courseId);


}
