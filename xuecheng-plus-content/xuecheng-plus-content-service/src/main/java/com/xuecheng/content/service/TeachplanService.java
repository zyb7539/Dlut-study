package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

public interface TeachplanService {
    /**
     * 根据id查询课程计划
     * @param courseId
     * */
    public List<TeachPlanDto> findTeachplanTree(Long courseId);

    /**
     * 新增/修改/保存 课程计划
     * @param saveTeachplanDto
     * */
    public void  saveTeachPlan(SaveTeachplanDto saveTeachplanDto);
}
