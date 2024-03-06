package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface CourseTeacherService {

    /**
     * 根据课程Id查询教师
     * @param courseId
     * */
    public List<CourseTeacher> getCourseTeacher(@PathVariable Long courseId);
    /**
     * 新增教师
     * @param companyId 机构Id
     * @param courseTeacherDto
     * */
    CourseTeacher createCourseTeacher(Long companyId, CourseTeacherDto courseTeacherDto);
    /**
     * 修改教师
     * @param courseTeacherDto
     * */
    CourseTeacher updateCourseTeacher(CourseTeacherDto courseTeacherDto);
    /**
     * 删除教师
     * @param companyId 机构Id
     * @param courseId
     * @param id
     * */
    void deleteCourseTeacher(Long companyId, Long courseId, Long id);
}
