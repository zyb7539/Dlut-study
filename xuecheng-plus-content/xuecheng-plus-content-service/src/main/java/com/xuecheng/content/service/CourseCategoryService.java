package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

import java.util.List;

/**
 * @description 课程分类接口
 * */
public interface CourseCategoryService {
    //课程分页查询
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
