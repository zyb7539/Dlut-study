package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        //调用mapper递归查询分类信息
        //list转为mapper，key为节点id，value为dto对象
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes("1");
        Map<String, CourseCategoryTreeDto> map = courseCategoryTreeDtos.stream().filter(item->!id.equals(item.getId())).collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
        //返回List
        ArrayList<CourseCategoryTreeDto> courseCategoryList = new ArrayList<>();
        courseCategoryTreeDtos.stream().filter(item->!id.equals(item.getId())).forEach(item -> {
            if(item.getParentid().equals(id)){
                courseCategoryList.add(item);
            }
            //找到节点的父节点
            CourseCategoryTreeDto parent = map.get(item.getParentid());
            if (parent != null){
                if(parent.getChildrenTreeNodes() == null){
                    parent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                parent.getChildrenTreeNodes().add(item);
            }
        });
        return courseCategoryList;
    }
}
