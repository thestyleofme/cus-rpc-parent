package com.github.thestyleofme.rpc.provider.handler;

import java.lang.reflect.Method;
import java.util.Optional;

import com.github.thestyleofme.rpc.common.pojo.RpcRequest;
import com.github.thestyleofme.rpc.common.utils.ApplicationContextHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.context.ApplicationContext;

/**
 * <p>
 * 自定义业务处理器
 * </p>
 *
 * @author isaac 2020/10/20 1:02
 * @since 1.0.0
 */
public class UserServiceHandler extends ChannelInboundHandlerAdapter {

    /**
     * 当客户端读取数据时，该方法被调用
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 注意：客户端将来发送请求时会传递一个参数：UserService#sayHello#are you ok
        // 判断当前请求是否符合规则
        if (msg instanceof RpcRequest) {
            // 获取消息对象
            RpcRequest request = (RpcRequest) msg;
            Class<?> clazz = request.getClazz();
            String methodName = request.getMethodName();
            // 获取Bean对象
            ApplicationContext context = Optional.ofNullable(ApplicationContextHelper.getContext())
                    .orElseThrow(() -> new IllegalStateException("not spring env, cannot get ApplicationContext"));
            Object serviceBean = context.getBean(clazz);
            // 获取方法并执行
            Method method = serviceBean.getClass().getMethod(methodName, request.getParameterTypes());
            Object result = method.invoke(serviceBean, request.getParameters());
            // 向客户端输出结果
            ctx.writeAndFlush(result);
        }
    }
}
