package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.ToString;

/**
 * @description 课程查询参数Dto
 */
@Data
@ToString
public class QueryCourseParamsDto {
    @ApiModelProperty(value = "审核状态")
    //审核状态
    private String auditStatus;
    @ApiModelProperty(value = "课程名称")
    //课程名称
    private String courseName;
    @ApiModelProperty(value = "发布状态")
    //发布状态
    private String publishStatus;
}
