package com.rabbit.mall.shop.web.aspect;


import cn.hutool.core.collection.CollectionUtil;
import com.rabbit.mall.common.response.BaseResponseVO;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class MethodArgNotValidExceptionAspect {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public BaseResponseVO<?> handleException(MethodArgumentNotValidException ex) {
        List<ObjectError> errors = ex.getAllErrors();
        if (CollectionUtil.isEmpty(errors)) {
            return new BaseResponseVO<>(500, "内部错误");
        }
        String errorMsg = errors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(","));
        return new BaseResponseVO<>(400, errorMsg);
    }
}
