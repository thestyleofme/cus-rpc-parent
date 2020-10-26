package com.github.thestyleofme.rpc.common.pojo;

import lombok.Data;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 9:32
 * @since 1.0.0
 */
@Data
public class RpcRequest {

    /**
     * 请求对象的ID
     */
    private String requestId;

    /**
     * 类名
     */
    private String className;

    /**
     * Class
     */
    private Class<?> clazz;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 参数类型
     */
    private Class<?>[] parameterTypes;

    /**
     * 入参
     */
    private Object[] parameters;

    /**
     * server
     */
    private String server;
}
