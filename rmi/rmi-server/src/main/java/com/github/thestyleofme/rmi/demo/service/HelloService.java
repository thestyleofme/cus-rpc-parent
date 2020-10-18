package com.github.thestyleofme.rmi.demo.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.github.thestyleofme.rmi.demo.pojo.User;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/16 1:55
 * @since 1.0.0
 */
public interface HelloService extends Remote {

    /**
     * sayHello
     *
     * @param user User
     * @return String
     * @throws RemoteException RemoteException
     */
    String sayHello(User user) throws RemoteException;
}
