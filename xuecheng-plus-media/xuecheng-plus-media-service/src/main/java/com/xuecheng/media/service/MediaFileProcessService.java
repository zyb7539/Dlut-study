package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @version 1.0
 */
public interface MediaFileProcessService {

 /**
  * @description 根据分片参数获取待处理任务
  * @param shardTotal  分片总数
  * @param shardindex  分片序号
  * @param count 任务数
  * @return java.util.List<com.xuecheng.media.model.po.MediaProcess>
  */
 List<MediaProcess> selectListByShardIndex(int shardTotal,  int shardIndex,  int count);
 /**
  *  开启一个任务
  * @param id 任务id
  * @return true开启任务成功，false开启任务失败
  */
 public boolean startTask(long id);

 /**
  * @description 保存任务结果
  * @param taskId  任务id
  * @param status 任务状态
  * @param fileId  文件id
  * @param url url
  * @param errorMsg 错误信息
  * @return void
  */
 void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);

}
