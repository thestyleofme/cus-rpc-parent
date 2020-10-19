package com.github.thestyleofme.rpc.provider.service;

import com.github.thestyleofme.rpc.common.service.UserService;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 0:52
 * @since 1.0.0
 */
public class UserServiceImpl implements UserService {

    @Override
    public String sayHello(String msg) {
        System.out.println("are you ok? " + msg);
        return "are you ok? " + msg;
    }

}
