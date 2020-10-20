package com.github.thestyleofme.rpc.common.codec;

import java.io.IOException;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 9:54
 * @since 1.0.0
 */
public interface Serializer {

    /**
     * java对象转换为二进制
     *
     * @param object Object
     * @return byte[]
     * @throws IOException IOException
     */
    byte[] serialize(Object object) throws IOException;

    /**
     * 二进制转换成java对象
     *
     * @param clazz Class<T>
     * @param bytes byte[]
     * @param <T>   T
     * @return T
     * @throws IOException IOException
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException;
}
