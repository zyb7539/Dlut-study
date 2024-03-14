package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;

import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.feignclient.searchclient.SearchServiceClient;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.po.CourseIndex;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * 课程发布任务类
 * */
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {
    @Autowired
    CoursePublishService coursePublishService;
    @Autowired
    SearchServiceClient searchServiceClient;
    @Autowired
    CoursePublishMapper coursePublishMapper;

    // 任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception{
        //分片参数
        int shardIndex = XxlJobHelper.getShardIndex();  //序号
        int shardTotal = XxlJobHelper.getShardTotal();  //分片数
        process(shardIndex,shardTotal,"course_publish",30,60);
    }
    // 执行课程抛出异常，说明执行失败
    @Override
    public boolean execute(MqMessage mqMessage) {
        Long courseId = Long.valueOf(mqMessage.getBusinessKey1());
        //课程静态化上传minio
        generateCourseHtml(mqMessage,courseId);
        // 向elasticsearch写索引数据
        saveCourseIndex(mqMessage,courseId);
        // 向redis写缓存
        saveCourseCache(mqMessage,courseId);
        //返回true，表示成功
        return true;
    }
    private void generateCourseHtml(MqMessage mqMessage,long courseId){
        //消息id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        //任务幂等性处理
        int stageOne = mqMessageService.getStageOne(taskId);
        if(stageOne > 0){
            log.debug("课程静态化任务已完成，无需处理....");
            return;
        }
        //开始进行课程静态化,生成html
        File file = coursePublishService.generateCourseHtml(courseId);
        if(file == null){
            XueChengPlusException.cast("生成的静态页面为空");
        }
        //上传minio
        coursePublishService.uploadCourseHtml(courseId,file);
        //设置任务已完成
        mqMessageService.completedStageOne(taskId);
    }
    // 保存课程索引信息
    private void saveCourseIndex(MqMessage mqMessage,long courseId){
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //任务幂等性处理
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if(stageTwo > 0){
            log.debug("保存课程索引信息已完成，无需处理....");
            return;
        }
        //向课程搜索服务
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        //远程调用
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);
        Boolean add = searchServiceClient.add(courseIndex);
        if(!add){
            XueChengPlusException.cast("远程调用搜索服务，添加课程索引失败");
        }
        //设置任务已完成
        mqMessageService.completedStageTwo(taskId);
    }
    //将课程信息缓存至redis
    private void saveCourseCache(MqMessage mqMessage,long courseId){
        log.debug("将课程信息缓存至redis,课程id:{}",courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}
