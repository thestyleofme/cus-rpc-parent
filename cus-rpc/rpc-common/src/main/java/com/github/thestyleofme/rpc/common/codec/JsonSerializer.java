package com.github.thestyleofme.rpc.common.codec;

import java.io.IOException;

import com.alibaba.fastjson.JSON;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 9:57
 * @since 1.0.0
 */
public class JsonSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) throws IOException {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException {
        return JSON.parseObject(bytes, clazz);
    }
}
