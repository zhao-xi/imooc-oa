package com.imooc.oa.service;

import com.imooc.oa.entity.Node;
import com.imooc.oa.entity.User;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.List;

public class UserServiceTest {
    private UserService userService = new UserService();
    @Test
    public void checkLogin1() {
        User user = userService.checkLogin("uu", "fasf");
    }
    @Test
    public void checkLogin2() {
        User user = userService.checkLogin("m8", "asdf");
    }
    @Test
    public void checkLogin3() {
        User user = userService.checkLogin("m8", "test");
    }


    @Test
    public void testSelectNodeByUserId() {
        List<Node> list = userService.selectNodeByUserId(2l);
        for(Node node : list) {
            System.out.println(node.getNodeName());
        }
    }
}