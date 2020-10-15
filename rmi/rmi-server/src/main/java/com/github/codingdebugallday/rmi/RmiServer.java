package com.github.codingdebugallday.rmi;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import com.github.codingdebugallday.rmi.demo.service.HelloService;
import com.github.codingdebugallday.rmi.demo.service.impl.HelloServiceImpl;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/16 2:05
 * @since 1.0.0
 */
public class RmiServer {

    public static void main(String[] args) throws RemoteException, AlreadyBoundException, MalformedURLException {
        // 创建HelloService实例
        HelloService helloService = new HelloServiceImpl();
        // 获取注册表
        LocateRegistry.createRegistry(8888);
        // 对象的绑定
        // 参数1：rmi://IP:PORT/服务名
        // 参数2：绑定的对象
        Naming.bind("//127.0.0.1:8888/rmiServer",helloService);
    }
}
