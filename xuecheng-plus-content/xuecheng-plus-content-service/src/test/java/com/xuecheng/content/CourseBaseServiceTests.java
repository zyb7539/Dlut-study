package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CourseBaseServiceTests {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Test
    public void testCourseBaseTests(){
        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
        courseParamsDto.setCourseName("java");
        courseParamsDto.setAuditStatus("202004"); //课程审核通过
        // 当前页码，每页记录数
        PageParams pageParams = new PageParams(1L, 2L);
        Page page = new Page(pageParams.getPageNo(), pageParams.getPageSize());

        PageResult<CourseBase> result = courseBaseInfoService.queryCourseBaseList(pageParams, courseParamsDto);
        System.out.println(result);
    }
}
