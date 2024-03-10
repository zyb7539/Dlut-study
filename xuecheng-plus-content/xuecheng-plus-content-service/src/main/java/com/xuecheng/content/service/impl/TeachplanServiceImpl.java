package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachPlanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;


    @Override
    public List<TeachPlanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    public void saveTeachPlan(SaveTeachplanDto saveTeachplanDto) {
        //通过课程计划的Id判断是新增还是修改
        Long teachplanId = saveTeachplanDto.getId();
        if(teachplanId == null){
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            Integer count = getTeachPlanCount(saveTeachplanDto);
            teachplan.setOrderby(count+1);

            teachplanMapper.insert(teachplan);
        }else {
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    @Transactional
    @Override
    public void deleteTeachplan(Long id) {
        // 删除第一级别的大章节时要求大章节下边没有小章节时方可删除。
        // 删除第二级别的小章节的同时需要将teachplan_media表关联的信息也删除。
        //查询章节，判断是大章节还是小章节
        Teachplan teachplan = teachplanMapper.selectById(id);
        if(teachplan.getGrade().equals(1)){
            //大章节
            //判断是否存在小章节
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid,id).eq(Teachplan::getCourseId,teachplan.getCourseId());
            Integer count = teachplanMapper.selectCount(queryWrapper);
            if(count != 0){
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            }else {
                teachplanMapper.deleteById(id);
            }
        }else {
            // 小章节
            //找到关联的媒体信息
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeachplanMedia::getTeachplanId,id).eq(TeachplanMedia::getCourseId,teachplan.getCourseId());
            teachplanMapper.deleteById(id);
            teachplanMediaMapper.delete(queryWrapper);

        }
    }
    @Transactional
    @Override
    public void moveTeachplan(String move, Long id) {
        //判断是上移还是下移
        //找到所有同一大章节下的子章节
        Teachplan teachplan = teachplanMapper.selectById(id);
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid,teachplan.getParentid()).eq(Teachplan::getCourseId,teachplan.getCourseId())
                .orderBy(true,true,Teachplan::getOrderby);
        List<Teachplan> teachplanList = teachplanMapper.selectList(queryWrapper);
        //找到移动章节所在的位置
        int location = 0;
        for (int i = 0; i < teachplanList.size(); i++) {
            if(teachplanList.get(i).getId().equals(id)){
                location = i;
                break;
            }
        }
        Teachplan teachplanOld = new Teachplan();
        //交换orderId
        if("moveup".equals(move)){
            //上移
            if(location == 0){
               return;
            }
             teachplanOld = teachplanList.get(location - 1);

        }else {
            //下移

            if(location ==teachplanList.size() - 1){
                return;
            }
            teachplanOld = teachplanList.get(location + 1);

        }
        Integer swap = teachplanOld.getOrderby();
        teachplanOld.setOrderby(teachplan.getOrderby());
        teachplan.setOrderby(swap);
        teachplanMapper.updateById(teachplanOld);
        teachplanMapper.updateById(teachplan);
    }

    @Transactional
    @Override
    public void associationMedia(BindTeachPlanMediaDto bindTeachPlanMediaDto) {
        //教学计划id
        Long teachplanId = bindTeachPlanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan==null){
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if(grade!=2){
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }
        //课程id
        Long courseId = teachplan.getCourseId();

        //先删除原有记录，再添加新纪录
        LambdaQueryWrapper<TeachplanMedia> wrapper =new LambdaQueryWrapper<>();
        wrapper.eq(TeachplanMedia::getTeachplanId,bindTeachPlanMediaDto.getTeachplanId());
        int delete = teachplanMediaMapper.delete(wrapper);

        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachPlanMediaDto,teachplanMedia);
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setMediaFilename(bindTeachPlanMediaDto.getFileName());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);

    }

    @Override
    public void deleteAssociationMedia(Long teachPlanId, String mediaId) {
        //先找课程计划
        LambdaQueryWrapper<TeachplanMedia> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeachplanMedia::getMediaId,mediaId).eq(TeachplanMedia::getTeachplanId,teachPlanId);
        TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(wrapper);
        if(teachplanMedia != null){
            //删除操作
            int i = teachplanMediaMapper.deleteById(teachplanMedia.getId());
            if( i <= 0){
                XueChengPlusException.cast("删除课程与媒资绑定计划失败");
            }
        }
    }

    private Integer getTeachPlanCount(SaveTeachplanDto saveTeachplanDto) {
        //确定排序字段
        Long parentid = saveTeachplanDto.getParentid();
        Long courseId = saveTeachplanDto.getCourseId();
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid,parentid).eq(Teachplan::getCourseId,courseId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }
}
