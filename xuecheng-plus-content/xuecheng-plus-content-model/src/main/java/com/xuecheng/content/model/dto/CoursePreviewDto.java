package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 课程预览模型类
 * */
@Data
@ToString
public class CoursePreviewDto {
    //课程基本信息，课程营销信息
    private CourseBaseInfoDto courseBase;

    //课程计划
    private List<TeachPlanDto> teachplans;
}
