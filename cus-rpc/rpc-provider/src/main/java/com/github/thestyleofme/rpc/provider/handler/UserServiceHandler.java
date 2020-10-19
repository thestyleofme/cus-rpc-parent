package com.github.thestyleofme.rpc.provider.handler;

import com.github.thestyleofme.rpc.provider.service.UserServiceImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

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
        String msgStr = msg.toString();
        if (msgStr.startsWith("UserService")) {
            // 如果符合规则，调用实现类获取到一个result
            UserServiceImpl userService = new UserServiceImpl();
            String result = userService.sayHello(msgStr.substring(msgStr.lastIndexOf("#") + 1));
            // result写到客户端
            ctx.writeAndFlush(result);
        }

    }
}
