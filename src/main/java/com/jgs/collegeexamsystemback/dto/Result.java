package com.jgs.collegeexamsystemback.dto;

import lombok.Data;


/**
* @description 全局统一返回结果类
* @returnType
* @author Administrator
* @date  18:30
*/

@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;


    public Result(){}

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, T data) {
        this.code = code;
        this.data = data;
    }


    protected static <T> Result<T> build(T data){
        Result<T> result = new Result<>();
        if (data != null){
            result.setData(data);
        }
        return result;
    }

    public static <T> Result<T> build(T body, ResultCodeEnum resultCodeEnum){
        Result<T> result = build(body);
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }

    public static <T> Result<T> build(Integer code,String message){
        Result<T> result = build(null);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /***
    * @description 操作成功
    * @returnType com.yygh.common.result.Result<T>
    * @author Administrator
    * @date  20:04
    */
    public static<T> Result<T> ok(T data){
        Result<T> result = build(data);
        return build(data,ResultCodeEnum.SUCCESS);
    }

    public static<T> Result<T> ok(){return Result.ok(null);}
    public static<T> Result<T> fail(){
        return Result.fail(null);
    }

    /***
    * @description 操作失败
    * @returnType com.yygh.common.result.Result<T>
    * @author Administrator
    * @date  20:07
    */
    public static<T> Result<T> fail(T data){
        Result<T> result = build(data);
        return build(data,ResultCodeEnum.FAIL);
    }

    public Result<T> message(String msg){
        this.setMessage(msg);
        return this;
    }

    public Result<T> code(Integer code){
        this.setCode(code);
        return this;
    }

    public boolean isOk() {
        return this.getCode().intValue() == ResultCodeEnum.SUCCESS.getCode().intValue();
    }
}
