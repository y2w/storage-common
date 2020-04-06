package com.github.msa.endpoint.response;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS                         (200,       "方法调用正常"),
    
    ENTITY_NOT_FOUND                (2001,      "在所选内容中没有找到数据或对象不存在"),
    SERVER_FAIL                     (400,       "应用级异常"),
    PARAM_ILLEGAL                   (401,       "参数验证出错"),
    NO_PERMISSION                   (404,       "请求服务未定义"),
    TOKEN_EXPIRED                   (403,       "token已失效"),
    PASSWORD_WRONG                  (405,       "密码错误"),
    IP_UNAUTHORIZED                 (406,       "客户端合法性验证出错"),
    APP_FAIL                        (500,       "调用服务异常"),
    NOT_CONNECTED                   (502,       "网络未连接或服务器未启动,请联系管理员"),
    UNSUPPORTED_PROTOCOL            (408,       "客户端请求的协议不正确"),
    UNSUPPORTED_ENCODING            (407,       "客户端数据编码不正确");

    // @formatter:on

    private int code;
    private String msg;

    private ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
