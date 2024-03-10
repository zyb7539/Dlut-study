package com.xuecheng.content.service.jobhandler;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 课程发布任务类
 * */
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {
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


        //返回true，表示成功
        return false;
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
        //开始进行课程静态化
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

        //设置任务已完成
        mqMessageService.completedStageTwo(taskId);
    }
    //将课程信息缓存至redis
    private void saveCourseCache(MqMessage mqMessage,long courseId){

    }
}
