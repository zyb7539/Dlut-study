package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.feignclient.contentclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.po.CoursePublish;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MyCourseTablesServiceImpl implements MyCourseTablesService {
    @Autowired
    XcChooseCourseMapper xcChooseCourseMapper;
    @Autowired
    XcCourseTablesMapper xcCourseTablesMapper;
    @Autowired
    ContentServiceClient contentServiceClient;

    @Transactional
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        // 选课调用内容查询课程收费规则
        CoursePublish coursePublish = contentServiceClient.getCoursePublish(courseId);
        if(coursePublish == null){
            XueChengPlusException.cast("课程信息不存在");
        }
        // 收费规则
        String charge = coursePublish.getCharge();
        XcChooseCourse xcChooseCourse = null;
        if ("201000".equals(charge)){
            //免费
             xcChooseCourse = addFreeCoruse(userId, coursePublish);
            XcCourseTables xcCourseTables = addCourseTabls(xcChooseCourse);
        }else {
            //收费
             xcChooseCourse = addChargeCoruse(userId, coursePublish);
        }
        // 判断学生学习规则
        XcCourseTablesDto learningStatus = getLearningStatus(userId, courseId);
        //构造返回值
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse,xcChooseCourseDto);
        xcChooseCourseDto.setLearnStatus(learningStatus.getLearnStatus());
        return xcChooseCourseDto ;
    }

    @NotNull
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        XcCourseTablesDto courseTablesDto = new XcCourseTablesDto();
        if(xcCourseTables == null){
            courseTablesDto.setLearnStatus("702002");
            return courseTablesDto;
        }
        boolean before = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if(before){
            //过期
            BeanUtils.copyProperties(xcCourseTables,courseTablesDto);
            courseTablesDto.setLearnStatus("702003");
            return courseTablesDto;
        }else {
            BeanUtils.copyProperties(xcCourseTables,courseTablesDto);
            courseTablesDto.setLearnStatus("702001");
            return courseTablesDto;
        }
    }

    @Override
    public PageResult<XcCourseTables> mycourestables(MyCourseTableParams params) {
        String userId = params.getUserId();
        int pageNo = params.getPage();
        int size = params.getSize();
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcCourseTables::getUserId,userId);
        // params.get
        // queryWrapper.orderBy();

        IPage<XcCourseTables> page = new Page<>(pageNo,size);
        IPage<XcCourseTables> xcCourseTablesIPage = xcCourseTablesMapper.selectPage(page, queryWrapper);

        return new PageResult<>(xcCourseTablesIPage.getRecords(),
                xcCourseTablesIPage.getTotal(),
                pageNo, size);
    }

    //添加免费课程,免费课程加入选课记录表、我的课程表
    public XcChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {
        Long courseId = coursepublish.getId();
        LambdaQueryWrapper<XcChooseCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(XcChooseCourse::getCourseId,courseId)
                .eq(XcChooseCourse::getUserId,userId)
                        .eq(XcChooseCourse::getOrderType,"700001")  //免费课程
                                .eq(XcChooseCourse::getStatus,"701001");    //选课成功
        //判断，存在了就直接返回
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(wrapper);
        if(xcChooseCourses.size() > 0){
            return xcChooseCourses.get(0);
        }
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setValidDays(365);
        xcChooseCourse.setStatus("701001");
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));

        int insert = xcChooseCourseMapper.insert(xcChooseCourse);
        if(insert <= 0){
            XueChengPlusException.cast("添加选课记录失败");
        }
        return xcChooseCourse;
    }

    //添加收费课程
    public XcChooseCourse addChargeCoruse(String userId, CoursePublish coursepublish){
        Long courseId = coursepublish.getId();
        LambdaQueryWrapper<XcChooseCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(XcChooseCourse::getCourseId,courseId)
                .eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getOrderType,"700002")  //收费课程
                .eq(XcChooseCourse::getStatus,"701002");    //带支付
        //判断，存在了就直接返回
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(wrapper);
        if(xcChooseCourses.size() > 0){
            return xcChooseCourses.get(0);
        }
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setValidDays(365);
        xcChooseCourse.setStatus("701002");
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));

        int insert = xcChooseCourseMapper.insert(xcChooseCourse);
        if(insert <= 0){
            XueChengPlusException.cast("添加选课记录失败");
        }
        return xcChooseCourse;
    }
    //添加到我的课程表
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse){

        //选课记录完成且未过期可以添加课程到课程表
        String status = xcChooseCourse.getStatus();
        if (!"701001".equals(status)){
            XueChengPlusException.cast("选课未成功，无法添加到课程表");
        }
        //查询我的课程表
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if(xcCourseTables!=null){
            return xcCourseTables;
        }
        XcCourseTables xcCourseTablesNew = new XcCourseTables();
        xcCourseTablesNew.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTablesNew.setUserId(xcChooseCourse.getUserId());
        xcCourseTablesNew.setCourseId(xcChooseCourse.getCourseId());
        xcCourseTablesNew.setCompanyId(xcChooseCourse.getCompanyId());
        xcCourseTablesNew.setCourseName(xcChooseCourse.getCourseName());
        xcCourseTablesNew.setCreateDate(LocalDateTime.now());
        xcCourseTablesNew.setValidtimeStart(xcChooseCourse.getValidtimeStart());
        xcCourseTablesNew.setValidtimeEnd(xcChooseCourse.getValidtimeEnd());
        xcCourseTablesNew.setCourseType(xcChooseCourse.getOrderType());
        int insert = xcCourseTablesMapper.insert(xcCourseTablesNew);
        if(insert <= 0){
            XueChengPlusException.cast("添加我的课程失败");
        }
        return xcCourseTablesNew;

    }

    /**
     * @description 根据课程和用户查询我的课程表中某一门课程
     * @param userId
     * @param courseId
     * @return com.xuecheng.learning.model.po.XcCourseTables
     */
    public XcCourseTables getXcCourseTables(String userId,Long courseId){
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId).eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;
    }

}
