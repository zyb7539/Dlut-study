package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.omg.SendingContext.RunTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class VideoTask {
    @Autowired
    MediaFileProcessService mediaFileProcessService;
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;
    @Autowired
    MediaFileService mediaFileService;


    //TODO 重点代码
    @XxlJob("VideoJobHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();  //序号
        int shardTotal = XxlJobHelper.getShardTotal();  //分片数
        //查询任务
        //cpu核心数
        int processors = Runtime.getRuntime().availableProcessors();
        List<MediaProcess> mediaProcessList = mediaFileProcessService.selectListByShardIndex(shardTotal, shardIndex, processors);
        //任务数量
        int size = mediaProcessList.size();
        log.debug("取到的视频任务数：{}",size);
        if(size <= 0){
            return;
        }
        //创建线程池执行
        ExecutorService executorService = Executors.newFixedThreadPool(size);

        //使用计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            //将任务加入线程池
            executorService.execute(() -> {
                try {
                    //任务执行逻辑
                    Long taskId = mediaProcess.getId();
                    String fileId = mediaProcess.getFileId();
                    boolean b = mediaFileProcessService.startTask(taskId);
                    if (!b) {
                        log.error("抢占任务失败，任务id：{}", taskId);
                        return;
                    }
                    //开启任务
                    //源avi视频的路径
                    String bucket = mediaProcess.getBucket();
                    String objectName = mediaProcess.getFilePath();
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    if (file == null) {
                        log.error("下载视频出错,任务id：{}，bucket：{}，objectname：{}", taskId, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频出错");
                        return;
                    }
                    String video_path = file.getAbsolutePath();
                    //转换后mp4文件的名称
                    String mp4_name = fileId + ".mp4";
                    //转换后mp4文件的路径
                    //先创建一个临时文件
                    File tempFile = null;
                    try {
                        tempFile = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.error("创建临时文件异常,{}", e.getMessage());
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
                        return;
                    }
                    String mp4_path = tempFile.getAbsolutePath();
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, video_path, mp4_name, mp4_path);
                    //开始视频转换，成功将返回success
                    String result = videoUtil.generateMp4();
                    if (!result.equals("success")) {
                        log.error("视频转码失败,原因：{}，bucket：{}，objectname：{}", result, bucket, objectName);

                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, result);
                        return;
                    }
                    //上传minio
                    String objectName1 = getFilePathByMd5(fileId, ".mp4");

                    boolean b1 = mediaFileService.addMediaFilesToMinIo(mp4_path, "video/mp4", bucket, objectName1);
                    if (!b1) {
                        log.error("上传MP4到minio失败,任务id：{}", taskId);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传MP4到minio失败");
                        return;
                    }
                    //mp4的url
                    //访问url
                    String url = "/" + bucket + "/" + objectName1;
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, "创建临时文件异常");
                }finally {  //避免出现异常的时候，计数器没有减1
                    //计数器减1
                    countDownLatch.countDown();
                }

            });
        });

        // 阻塞,指定最大限度的等待时间
        countDownLatch.await(30, TimeUnit.MINUTES);
    }
    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }
}
