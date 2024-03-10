package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachPlanMediaDto;
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

    /**
     * 根据章节id删除对应章节
     * @param id
     * */
    public void  deleteTeachplan(Long id);
    /**
     * 移动章节
     * @param move
     * @param id
     * */
    void moveTeachplan(String move, Long id);

    public void associationMedia(BindTeachPlanMediaDto bindTeachPlanMediaDto);

    /**
     * 删除媒资绑定课程计划
     * @param mediaId
     * @param teachPlanId
     * */
    void deleteAssociationMedia(Long teachPlanId, String mediaId);
}
