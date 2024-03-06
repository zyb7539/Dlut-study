package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * @description 课程信息管理接口
 * */
public interface CourseBaseInfoService {
    //课程分页查询
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);
    /**
     * 新增课程
     * @param companyId 机构Id
     * @param addCourseDto 课程信息
     * @return 课程详细信息
     * */
    public CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto addCourseDto);
    /**
     * 根据课程Id查询课程
     * @return 课程详细信息
     */
    public CourseBaseInfoDto  getCourseBaseInfo(Long courseId);

    /**
     * 修改课程信息
     * @param companyId 机构Id
     * @param editCourseDto 修改信息
     * @return 课程详细信息
     */
    public CourseBaseInfoDto  updateCourseBase(Long companyId, EditCourseDto editCourseDto);
    /**
     * 删除课程
     * @param id
     */
    void deleteCourseBase(Long id);
}
