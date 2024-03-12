package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachPlanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description 课程计划管理接口
 * */
@Api("课程计划管理接口")
@RestController
public class TeachPlanController {
    @Autowired
    private TeachplanService teachplanService;

    @ApiOperation("查询课程计划编辑接口")
    @GetMapping("teachplan/{courseId}/tree-nodes")
    public List<TeachPlanDto> getTreeNodes(@PathVariable Long courseId){
        return teachplanService.findTeachplanTree(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan( @RequestBody @Validated SaveTeachplanDto teachplan){
        teachplanService.saveTeachPlan(teachplan);
    }

    @ApiOperation("删除章节")
    @DeleteMapping("/teachplan/{id}")
    public void deleteTeachplan(@PathVariable Long id){
        teachplanService.deleteTeachplan(id);
    }
    @ApiOperation("移动章节")
    @PostMapping("/teachplan/{move}/{id}")
    public void moveTeachplan(@PathVariable String move,@PathVariable Long id){
        teachplanService.moveTeachplan(move,id);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachPlanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }
    @ApiOperation(value = "课程计划和媒资信息绑定删除")
    @DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
    public void deleteAssociationMedia(@PathVariable Long teachPlanId,@PathVariable String mediaId){
        teachplanService.deleteAssociationMedia(teachPlanId,mediaId);

    }


}