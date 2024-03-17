package com.xuecheng.learning.service.impl;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.feignclient.contentclient.ContentServiceClient;
import com.xuecheng.feignclient.meidaclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.po.CoursePublish;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LearningServiceImpl implements LearningService {
    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Autowired
    ContentServiceClient contentServiceClient;
    @Autowired
    MediaServiceClient mediaServiceClient;
    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        CoursePublish coursePublish = contentServiceClient.getCoursePublish(courseId);
        // 查询课程信息
        if(coursePublish ==null){
            return RestResponse.validfail("课程不存在");
        }
        // 根据课程计划id，查询课程计划信息，如果is_preview为1表示支持试学
        String teachplan = coursePublish.getTeachplan();
        //todo 是否支持死穴
        if(StringUtils.isNotEmpty(userId)){
            // 获取学习资格
            XcCourseTablesDto learningStatus = myCourseTablesService.getLearningStatus(userId, courseId);
            String learnStatus = learningStatus.getLearnStatus();
            if("702002".equals(learnStatus)){
                return RestResponse.validfail("无法学习,因为没有选课或选课后没有支付");
            } else if ("702003".equals(learnStatus)) {
                return RestResponse.validfail("已过期需要申请续期或重新支付");
            }else {
                //有资格学习，视频播放地址
                RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
                return playUrlByMediaId;
            }
        }
        // 没有登录
        // 取出课程的收费规则
        String charge = coursePublish.getCharge();
        if("201000".equals(charge)){
            //免费
            RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
            return playUrlByMediaId;
        }
        return RestResponse.validfail("该课程没有选课");
    }
}
