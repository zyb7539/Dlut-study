package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.util.List;

/**
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

/**
 * @param localFilePath  本地文件路径
 * @param companyId 机构id
 * @param uploadFileParamsDto 文件信息
 * @param objectName  如果传入objectName就按照objectname去存储
 * */
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto,String localFilePath,String objectName);


 public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName);


 /**
  * @param fileMd5 文件的md5
  * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
  */
 public RestResponse<Boolean> checkFile(String fileMd5);

 /**
  * @description 检查分块是否存在
  * @param fileMd5  文件的md5
  * @param chunkIndex  分块序号
  * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
  */
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);


 /**
  * @description 上传分块
  * @param fileMd5  文件md5
  * @param chunk  分块序号
  * @param localChunkFilePath  分块文件本地路径
  * @return com.xuecheng.base.model.RestResponse
  */
 public RestResponse uploadChunk(String fileMd5,int chunk,String localChunkFilePath);

 /**
  * @description 合并分块
  * @param companyId  机构id
  * @param fileMd5  文件md5
  * @param chunkTotal 分块总和
  * @param uploadFileParamsDto 文件信息
  * @return com.xuecheng.base.model.RestResponse
  */
 public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);

 public File downloadFileFromMinIO(String bucket, String objectName);

 public boolean addMediaFilesToMinIo(String localFilePath,String mimeType,String bucket, String objectName);

   /**
    * 根据媒资id查询文件信息
    * @param mediaId
    * */
    MediaFiles getFileById(String mediaId);
}
