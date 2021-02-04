package com.imooc.oa.service;

import com.imooc.oa.dao.RbacDao;
import com.imooc.oa.dao.UserDao;
import com.imooc.oa.entity.Node;
import com.imooc.oa.entity.User;
import com.imooc.oa.service.exception.BussinessException;
import com.imooc.oa.utils.MD5Utils;

import java.util.List;

public class UserService {
    private UserDao userDao = new UserDao();
    private RbacDao rbacDao = new RbacDao();

    /**
     * 根据前台输入数据进行登录校验
     * @param username 前台输入的用户名
     * @param password 前台输入的密码
     * @return 校验通过后返回对应User实体类
     * @throws BussinessException L001-用户不存在，L002-密码错误
     */
    public User checkLogin(String username, String password) {
        User user = userDao.selectByUsername(username);
        if(user == null) {
            // 用户不存在，抛出异常
            throw new BussinessException("L001", "用户名不存在");
        }
        String md5 = MD5Utils.md5Digest(password, user.getSalt());
        if(!md5.equals(user.getPassword())) {
            // 密码错误，抛出异常
            throw new BussinessException("L002", "密码错误");
        }
        return user;
    }

    public List<Node> selectNodeByUserId(Long userId) {
        return rbacDao.selectNodeByUserId(userId);
    }
}
