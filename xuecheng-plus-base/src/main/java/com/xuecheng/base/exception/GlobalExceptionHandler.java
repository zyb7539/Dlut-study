package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 全局异常处理器
 * */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 项目自定义异常
     * */
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengPlusException e){
        String errMessage = e.getErrMessage();
        //记录日常
        log.error("系统日常：{}",errMessage,e);
        RestErrorResponse errorResponse = new RestErrorResponse(errMessage);
        return errorResponse;
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e){
        String errMessage = e.getMessage();
        //记录日常
        log.error("系统日常：{}",errMessage,e);
        RestErrorResponse errorResponse = new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
        return errorResponse;
    }

}
