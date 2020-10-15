package com.github.codingdebugallday.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.github.codingdebugallday.rmi.demo.pojo.User;
import com.github.codingdebugallday.rmi.demo.service.HelloService;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/16 2:05
 * @since 1.0.0
 */
public class RmiClient {

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        // 从注册表中获取远程对象，强转
        HelloService helloService = (HelloService) Naming.lookup("//127.0.0.1:8888/rmiServer");
        // 准备参数 这里的user必须跟服务端一致，包括serialVersionUID
        User user = new User("isaac", 24);
        // 调用远程方法 sayHello
        String result = helloService.sayHello(user);
        System.out.println(result);
    }
}