package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
//TODO 师资
@Service
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Autowired
    TeachplanService teachplanService;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    MqMessageService mqMessageService;
    @Autowired
    CourseTeacherService courseTeacherService;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        // 课程基本信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);
        // 课程计划信息
        List<TeachPlanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePreviewDto.setTeachplans(teachplanTree);

        return coursePreviewDto;
    }
    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        //查询课程基本，营销，计划等信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if(!companyId.equals(courseBaseInfo.getCompanyId())){
            XueChengPlusException.cast("只能提交本机构课程");
        }
        if(courseBaseInfo == null){
            XueChengPlusException.cast("课程找不到");
        }
        String auditStatus = courseBaseInfo.getAuditStatus();
        if("202003".equals(auditStatus)){
            XueChengPlusException.cast("课程已提交，请等待审核");
        }
        String pic = courseBaseInfo.getPic();
        if(pic == null){
            XueChengPlusException.cast("请上传课程图片信息");
        }
        //课程计划
        List<TeachPlanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if(teachplanTree == null || teachplanTree.isEmpty()){
            XueChengPlusException.cast("请编写课程计划");
        }
        // 营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if(courseMarket == null){
            XueChengPlusException.cast("请编写课程营销信息");
        }
        //插入预发布表
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        //转Json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        // 计划信息
        String teachplan = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplan);
        coursePublishPre.setStatus("202003");
        coursePublishPre.setCreateDate(LocalDateTime.now());
        // 教师信息
        List<CourseTeacher> courseTeacher = courseTeacherService.getCourseTeacher(courseId);
        String courseTeacherJson = JSON.toJSONString(courseTeacher);
        coursePublishPre.setTeachers(courseTeacherJson);
        //查询预发布表，有记录更新没有插入
        CoursePublishPre coursePublishPre1 = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre1 == null){
            coursePublishPreMapper.insert(coursePublishPre);
        }else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本信息表审核状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            XueChengPlusException.cast("课程预发布表为空");
        }
        if(!companyId.equals(coursePublishPre.getCompanyId())){
            XueChengPlusException.cast("只能发布自己机构的课程");
        }
        if(!coursePublishPre.getStatus().equals("202004")){
            XueChengPlusException.cast("课程没有审核不允许发布");
        }
        //向课程发布表写数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        //先查询课程发布表
        CoursePublish publish = coursePublishMapper.selectById(courseId);
        if(publish == null){
            coursePublishMapper.insert(coursePublish);
        }else {
            coursePublishMapper.updateById(coursePublish);
        }
        // 修改课程基本信息表
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //已发布
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
        //像课程消息表写数据
        saveCoursePublishMessage(courseId);
        //删除预发布表数据
        coursePublishPreMapper.deleteById(courseId);
    }

    /**
     * @description 保存消息表记录，稍后实现
     * @param courseId  课程id
     * @return void
     * @author Mr.M
     * @date 2022/9/20 16:32
     */
    private void saveCoursePublishMessage(Long courseId){
        MqMessage coursePublish = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(coursePublish ==null){
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }

    }

}
//TODO 如何审核;

