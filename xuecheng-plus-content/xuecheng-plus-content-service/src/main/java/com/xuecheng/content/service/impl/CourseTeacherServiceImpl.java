package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Override
    public List<CourseTeacher> getCourseTeacher(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId);

        return courseTeacherMapper.selectList(queryWrapper);
    }

    @Override
    public CourseTeacher createCourseTeacher(Long companyId, CourseTeacherDto courseTeacherDto) {
        //判断添加的课程的老师是否属于本机构
        CourseBase courseBase = courseBaseMapper.selectById(courseTeacherDto.getCourseId());
        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("该课程不属于本机构，无法增加教师！");
        }
        CourseTeacher courseTeacher = new CourseTeacher();

        //判断是新增还是修改
        if(courseTeacherDto.getId() == null){
            // 新增
            BeanUtils.copyProperties(courseTeacherDto,courseTeacher);
            courseTeacher.setCreateDate(LocalDateTime.now());
            courseTeacherMapper.insert(courseTeacher);
        }else {
            //修改
            courseTeacher = courseTeacherMapper.selectById(courseTeacherDto.getId());
            BeanUtils.copyProperties(courseTeacherDto,courseTeacher);
            courseTeacherMapper.updateById(courseTeacher);
        }

        return courseTeacher;
    }

    @Override
    public CourseTeacher updateCourseTeacher(CourseTeacherDto courseTeacherDto) {

        CourseTeacher courseTeacher = courseTeacherMapper.selectById(courseTeacherDto.getId());
        BeanUtils.copyProperties(courseTeacherDto,courseTeacher);
        courseTeacherMapper.updateById(courseTeacher);

        return courseTeacher;
    }

    @Override
    public void deleteCourseTeacher(Long companyId, Long courseId, Long id) {
        //判断添加的课程的老师是否属于本机构
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("该课程不属于本机构，无法增加教师！");
        }

        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId).eq(CourseTeacher::getId,id);
        int i = courseTeacherMapper.delete(queryWrapper);
        if(i <= 0){
            XueChengPlusException.cast("删除课程教师失败");
        }
    }
}
