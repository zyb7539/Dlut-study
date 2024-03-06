package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程教师信息
 * */
@Api("课程教师信息")
@RestController
public class CourseTeacherController {
    @Autowired
    private CourseTeacherService courseTeacherService;

    @ApiOperation("根据课程Id查询教师")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getCourseTeacher(@PathVariable Long courseId){
        return courseTeacherService.getCourseTeacher(courseId);

    }
    @ApiOperation("新增教师")
    @PostMapping("/courseTeacher")
    public CourseTeacher createCourseTeacher(@RequestBody @Validated(ValidationGroups.Insert.class)CourseTeacherDto courseTeacherDto){
        Long companyId = 1232141425L;
        return courseTeacherService.createCourseTeacher(companyId,courseTeacherDto);
    }

    @ApiOperation("修改教师")
    @PutMapping("/courseTeacher")
    public CourseTeacher updateCourseTeacher(@RequestBody @Validated(ValidationGroups.Insert.class)CourseTeacherDto courseTeacherDto){
        Long companyId = 1232141425L;
        return courseTeacherService.updateCourseTeacher(courseTeacherDto);
    }

    @ApiOperation("删除教师")
    @DeleteMapping("/courseTeacher/course/{courseId}/{id}")
    public void deleteCourseTeacher(@PathVariable Long courseId,@PathVariable Long id){
        Long companyId = 1232141425L;
        courseTeacherService.deleteCourseTeacher(companyId,courseId,id);
    }
}
