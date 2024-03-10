package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description 媒资服务业务层接口
 */
 @Service
 @Slf4j
public class MediaFileServiceImpl implements MediaFileService {

  @Autowired
 MediaFilesMapper mediaFilesMapper;
  @Autowired
 MinioClient minioClient;
  @Autowired
 MediaProcessMapper mediaProcessMapper;

  @Value("${minio.bucket.files}")
  private String bucketMediaFiles;
 @Value("${minio.bucket.videofiles}")
 private String bucketVideoFiles;
 @Autowired
 private MediaFileService currentProxy;

 @Override
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

  //构建查询条件对象
  LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
  
  //分页对象
  Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
  // 查询数据内容获得结果
  Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
  // 获取数据列表
  List<MediaFiles> list = pageResult.getRecords();
  // 获取数据总数
  long total = pageResult.getTotal();
  // 构建结果集
  PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
  return mediaListResult;

 }

 @Override
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
  String filename = uploadFileParamsDto.getFilename();
  //得到扩展名
  String extension = filename.substring(filename.lastIndexOf("."));
  String mimeType = getMimeType(extension);
  //文件上传到minio
  String defaultFolderPath = getDefaultFolderPath();
  //文件的md5值
  String fileMd5 = getFileMd5(new File(localFilePath));
  String objectName  = defaultFolderPath + fileMd5 + extension;
  boolean result = addMediaFilesToMinIo(localFilePath, mimeType, bucketMediaFiles, objectName);
  if(!result){
   XueChengPlusException.cast("上传文件失败");
  }
  //文件信息保存数据库
  MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucketMediaFiles, objectName);
  UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
  if(mediaFiles == null){
   XueChengPlusException.cast("文件上传后保存信息失败");
  }
  BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);
  return uploadFileResultDto;
 }

 public boolean addMediaFilesToMinIo(String localFilePath,String mimeType,String bucket, String objectName) {
  try {
   UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
           .bucket(bucket)
           .filename(localFilePath)
           .object(objectName) //在桶下放在子目录
           .contentType(mimeType)
           .build();
   //上传文件
   minioClient.uploadObject(uploadObjectArgs);
   log.debug("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectName);

   return true;
  } catch (Exception e) {
   e.printStackTrace();
   log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucket,objectName,e.getMessage(),e);
   return false;
  }
 }

 @Override
 public MediaFiles getFileById(String mediaId) {
  MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
  return mediaFiles;
 }

 // 根据扩展名获得mimeType
 private  String getMimeType(String extension) {
  if(extension == null){
   extension = "";
  }
  ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
  String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
  if(extensionMatch!=null){
   mimeType =  extensionMatch.getMimeType();
  }
  return mimeType;
 }
 //获取文件默认存储目录路径 年/月/日
 private String getDefaultFolderPath() {
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  String folder = sdf.format(new Date()).replace("-", "/")+"/";
  return folder;
 }
 //获取文件的md5
 private String getFileMd5(File file) {
  try (FileInputStream fileInputStream = new FileInputStream(file)) {
   String fileMd5 = DigestUtils.md5Hex(fileInputStream);
   return fileMd5;
  } catch (Exception e) {
   e.printStackTrace();
   return null;
  }
 }

 @Transactional
 public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
  //从数据库查询文件
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if (mediaFiles == null) {
   mediaFiles = new MediaFiles();
   //拷贝基本信息
   BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
   mediaFiles.setId(fileMd5);
   mediaFiles.setFileId(fileMd5);
   mediaFiles.setCompanyId(companyId);
   mediaFiles.setUrl("/" + bucket + "/" + objectName);
   mediaFiles.setBucket(bucket);
   mediaFiles.setFilePath(objectName);
   mediaFiles.setCreateDate(LocalDateTime.now());
   mediaFiles.setAuditStatus("002003");
   mediaFiles.setStatus("1");
   //保存文件信息到文件表
   int insert = mediaFilesMapper.insert(mediaFiles);
   if (insert < 0) {
    log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
    XueChengPlusException.cast("保存文件信息失败");
    return null;
   }
   log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());

  }
  // 记录待处理任务
  addWaitingTask(mediaFiles);

  return mediaFiles;

 }

 // 记录待处理任务
 private void addWaitingTask(MediaFiles mediaFiles){
  String filename = mediaFiles.getFilename();
  //文件扩展名
  String extension = filename.substring(filename.lastIndexOf("."));
  String mimeType = getMimeType(extension);
  if("video/x-msvideo".equals(mimeType)){//是avi视频写入待处理任务表
   MediaProcess mediaProcess = new MediaProcess();
   BeanUtils.copyProperties(mediaFiles,mediaProcess);
   mediaProcess.setStatus("1");
   mediaProcess.setFailCount(0); //默认失败值
   mediaProcess.setUrl(null);
   mediaProcessMapper.insert(mediaProcess);
  }

 }


 @Override
 public RestResponse<Boolean> checkFile(String fileMd5) {
  //先查询数据库，再查寻minio
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if(mediaFiles != null){
   //查询minio
   GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(mediaFiles.getBucket()).object(mediaFiles.getFilePath()).build();
   try {
    FilterInputStream re = minioClient.getObject(getObjectArgs);
    if (re != null){
     //文件以存在
     return RestResponse.success(true);
    }
   } catch (Exception e){
     e.printStackTrace();
    }
  }
  //文件不存在
  return RestResponse.success(false);
 }

 @Override
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
  //分块存储路径：md5前两位子目录，chunk存储分块文件
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

   //查询minio
   GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucketVideoFiles).object(chunkFileFolderPath + chunkIndex).build();
   try {
    FilterInputStream re = minioClient.getObject(getObjectArgs);
    if (re != null){
     //分块以存在
     return RestResponse.success(true);
    }
   } catch (Exception e){
    e.printStackTrace();
   }
  //分块不存在
  return RestResponse.success(false);
 }

 @Override
 public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
  //上传分块到minio
  String mimeType = getMimeType(null);
  //分文件的路径
  String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
  boolean toMinIo = addMediaFilesToMinIo(localChunkFilePath, mimeType, bucketVideoFiles, chunkFilePath);
  if (!toMinIo){
    return RestResponse.validfail(false,"上传文件失败");
  }
  //上传成功
  return RestResponse.success(true);
 }

 @Override
 public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
  //分块文件再目录
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
  //找到分块文件调用minio的sdk进行文件合并
  List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
          .limit(chunkTotal)
          .map(i -> ComposeSource.builder()
                  .bucket(bucketVideoFiles)
                  .object(chunkFileFolderPath + i)
                  .build())
          .collect(Collectors.toList());
  //合并后文件名称
  String filename = uploadFileParamsDto.getFilename();
  String extension = filename.substring(filename.lastIndexOf("."));
  String objectName = getFilePathByMd5(fileMd5, extension);
  ComposeObjectArgs testbucket = ComposeObjectArgs.builder().bucket(bucketVideoFiles)
          .sources(sources)
          .object(objectName).build();
  try {
   minioClient.composeObject(testbucket);
  } catch (Exception e) {
    e.printStackTrace();
    log.error("合并文件出错,bucket:{},objectName:{},错误信息:{}",bucketVideoFiles,objectName,e);
    return RestResponse.validfail(false,"合并文件异常");
  }
  //检验合并后的文件是否一致，视频上传成功
  //先下载文件
  File file = downloadFileFromMinIO(bucketVideoFiles, objectName);
  //合并后文件的md5
  try(FileInputStream fileInputStream = new FileInputStream(file)) {
   String mergeFile_md5 = DigestUtils.md5Hex(fileInputStream);
   if(!fileMd5.equals(mergeFile_md5)){
    log.error("校验合并文件md5值不一致，原始文件：{}，合并文件：{}",fileMd5,mergeFile_md5);
    return RestResponse.validfail(false,"文件校验失败");
   }
  }catch (Exception e){
   return RestResponse.validfail(false,"文件校验失败");
  }
  //文件大小
  uploadFileParamsDto.setFileSize(file.length());
  //将文件信息入库
  MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucketVideoFiles, objectName);
  if(mediaFiles == null){
   return RestResponse.validfail(false,"文件入库失败");
  }
  //清理分块文件
 clearChunkFiles(chunkFileFolderPath,chunkTotal);

  return RestResponse.success(true);
 }

 //得到分块文件的目录
 private String getChunkFileFolderPath(String fileMd5) {
  return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
 }

 /**
  * 得到合并后的文件的地址
  * @param fileMd5 文件id即md5值
  * @param fileExt 文件扩展名
  * @return
  */
 private String getFilePathByMd5(String fileMd5,String fileExt){
  return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
 }


 /**
  * 从minio下载文件
  * @param bucket 桶
  * @param objectName 对象名称
  * @return 下载后的文件
  */
 public File downloadFileFromMinIO(String bucket,String objectName){
  //临时文件
  File minioFile = null;
  FileOutputStream outputStream = null;
  try{
   InputStream stream = minioClient.getObject(GetObjectArgs.builder()
           .bucket(bucket)
           .object(objectName)
           .build());
   //创建临时文件
   minioFile=File.createTempFile("minio", ".merge");
   outputStream = new FileOutputStream(minioFile);
   IOUtils.copy(stream,outputStream);
   return minioFile;
  } catch (Exception e) {
   e.printStackTrace();
  }finally {
   if(outputStream!=null){
    try {
     outputStream.close();
    } catch (IOException e) {
     e.printStackTrace();
    }
   }
  }
  return null;
 }
 /**
  * 清除分块文件
  * @param chunkFileFolderPath 分块文件路径
  * @param chunkTotal 分块文件总数
  */
 private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal){

  try {
   List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
           .limit(chunkTotal)
           .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
           .collect(Collectors.toList());

   RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucketVideoFiles).objects(deleteObjects).build();
   Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
   //真正删除
   results.forEach(r->{
    DeleteError deleteError = null;
    try {
     deleteError = r.get();
    } catch (Exception e) {
     e.printStackTrace();
     log.error("清楚分块文件失败,objectname:{}",deleteError.objectName(),e);
    }
   });
  } catch (Exception e) {
   e.printStackTrace();
   log.error("清楚分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
  }


}}
