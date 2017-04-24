package com.kongzhong.mrpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RpcRequest {

    private String requestId;
    private Method method;
    private String className;
    private String methodName;
    private Class[] parameterTypes;
    private Object[] parameters;
    private Class<?> returnType;
    private RpcContext context;

    public RpcRequest(String requestId, Method method, String className, String methodName, Class[] parameterTypes, Object[] parameters, Class<?> returnType) {
        this.requestId = requestId;
        this.method = method;
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameters = parameters;
        this.returnType = returnType;
    }

}