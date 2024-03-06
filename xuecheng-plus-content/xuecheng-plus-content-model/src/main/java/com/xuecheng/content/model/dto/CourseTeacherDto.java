package com.xuecheng.content.model.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.xuecheng.base.exception.ValidationGroups;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 课程-教师关系表
 * </p>
 *
 * @author itcast
 */
@Data
@ToString
public class CourseTeacherDto{

    private Long id;

    /**
     * 课程标识
     */
    private Long courseId;

    /**
     * 教师标识
     */
    @NotEmpty(message = "新增教师标识不能为空",groups = {ValidationGroups.Insert.class})
    @NotEmpty(message = "修改教师标识不能为空",groups = {ValidationGroups.Update.class})
    private String teacherName;

    /**
     * 教师职位
     */
    @NotEmpty(message = "新增教师职位不能为空",groups = {ValidationGroups.Insert.class})
    @NotEmpty(message = "修改教师职位不能为空",groups = {ValidationGroups.Update.class})
    private String position;

    /**
     * 教师简介
     */
    private String introduction;

    /**
     * 照片
     */
    private String photograph;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate;


}
