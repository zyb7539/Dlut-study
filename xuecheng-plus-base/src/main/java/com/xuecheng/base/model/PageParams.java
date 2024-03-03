package com.xuecheng.base.model;

import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @description 分页查询分页参数
 * */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class  PageParams {
    @ApiParam(value = "页码")
    // 当前页码
    private Long pageNo = 1L;
    @ApiParam(value = "记录数")
    // 每页显示记录数
    private Long pageSize = 30L;
}
