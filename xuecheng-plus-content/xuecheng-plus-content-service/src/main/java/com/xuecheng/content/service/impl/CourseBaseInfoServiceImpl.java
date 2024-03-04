package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto courseParamsDto) {
        //拼装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(courseParamsDto.getCourseName()),CourseBase::getName,courseParamsDto.getCourseName());
        //根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotBlank(courseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,courseParamsDto.getAuditStatus());
        //根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotBlank(courseParamsDto.getPublishStatus()),CourseBase::getStatus,courseParamsDto.getPublishStatus());
        // 当前页码，每页记录数
        Page page = new Page(pageParams.getPageNo(), pageParams.getPageSize());
        //
        Page pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        PageResult<CourseBase> result = new PageResult<CourseBase>(pageResult.getRecords(),pageResult.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
       return result;
    }
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

        //向课程基本信息表course_base写入数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(dto,courseBase); //只要属性名一致就可以拷贝
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        int insert = courseBaseMapper.insert(courseBase);
        if(insert <=0){
            XueChengPlusException.cast("添加课程失败");
        }
        //向课程营销表course_market写入数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);
        courseMarket.setId(courseBase.getId());
        //保存营销信息
        saveCourseMarket(courseMarket);
        //从数据库查询课程详细信息

        return getCourseBaseInfo(courseBase.getId());
    }


    // 保存营销信息
    private int saveCourseMarket(CourseMarket courseMarket){
        int result;
        //参数的合法性校验
        String charge = courseMarket.getCharge();
        if(StringUtils.isEmpty(charge)){
            XueChengPlusException.cast("收费规则为空");
        }
        if(charge.equals("201001")){
            if(courseMarket.getOriginalPrice() == null || courseMarket.getOriginalPrice().floatValue() <= 0){
                XueChengPlusException.cast("课程的价格不能为空且必须大于0");
            }
            if(courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0){
                XueChengPlusException.cast("课程的价格不能为空且必须大于0");
            }
        }
        CourseMarket courseMarketOld = courseMarketMapper.selectById(courseMarket.getId());
        if(courseMarketOld == null){
            //插入数据库
           result = courseMarketMapper.insert(courseMarket);
        }else {
            BeanUtils.copyProperties(courseMarket,courseMarketOld);
           result = courseMarketMapper.updateById(courseMarket);
        }
        return result;
    }
    //从数据库查询课程详细信息
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        //课程分类名称
        LambdaQueryWrapper<CourseCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseCategory::getId,courseBaseInfoDto.getMt())
                .or()
                .eq(CourseCategory::getId,courseBaseInfoDto.getSt());
        List<CourseCategory> courseCategories = courseCategoryMapper.selectList(queryWrapper);
        for (CourseCategory courseCategory : courseCategories) {
            if (courseCategory.getId().equals(courseBaseInfoDto.getMt())){
                courseBaseInfoDto.setMtName(courseCategory.getName());
            }else {
                courseBaseInfoDto.setStName(courseCategory.getName());
            }
        }
        return courseBaseInfoDto;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        //拿到课程Id
        Long courseId = editCourseDto.getId();
        //查询课程
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            XueChengPlusException.cast("课程不存在");
        }
        //业务逻辑校验
        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }
        //封装数据
        BeanUtils.copyProperties(editCourseDto,courseBase);
        //修改时间
        courseBase.setCreateDate(LocalDateTime.now());

        //跟新基本信息
        int i = courseBaseMapper.updateById(courseBase);
        if(i <= 0){
            XueChengPlusException.cast("修改课程基本信息失败");
        }
        //更新营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        i = saveCourseMarket(courseMarket);
        if(i <= 0){
            XueChengPlusException.cast("修改课程营销信息失败");
        }
        return getCourseBaseInfo(courseId);
    }
}