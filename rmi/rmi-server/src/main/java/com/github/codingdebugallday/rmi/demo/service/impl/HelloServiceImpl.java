package com.github.codingdebugallday.rmi.demo.service.impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.github.codingdebugallday.rmi.demo.pojo.User;
import com.github.codingdebugallday.rmi.demo.service.HelloService;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/16 1:59
 * @since 1.0.0
 */
public class HelloServiceImpl extends UnicastRemoteObject implements HelloService {

    private static final long serialVersionUID = -7515462130667106145L;

    /**
     * 手动实现父类的构造方法
     *
     * @throws RemoteException RemoteException
     */
    public HelloServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public String sayHello(User user) throws RemoteException {
        System.out.println("this is server, say hello to " + user.getUsername());
        return "success";
    }
}
