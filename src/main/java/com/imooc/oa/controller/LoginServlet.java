package com.imooc.oa.controller;

import com.alibaba.fastjson.JSON;
import com.imooc.oa.entity.User;
import com.imooc.oa.service.UserService;
import com.imooc.oa.service.exception.BussinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "LoginServlet", urlPatterns = "/check_login")
public class LoginServlet extends HttpServlet {
    private UserService userService = new UserService();
    Logger logger = LoggerFactory.getLogger(LoginServlet.class);
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.checkLogin(username, password);
            // 向session中存入用户登录信息
            HttpSession session = request.getSession();
            session.setAttribute("login_user", user);
            result.put("code", "0");
            result.put("message", "success");
            result.put("redirect_url", "/index");
        } catch (BussinessException ex) {
            logger.error(ex.getMessage(), ex);
            result.put("code", ex.getCode());
            result.put("message", ex.getMessage());
        } catch (Exception e) {
            result.put("code", e.getClass().getSimpleName());
            result.put("message", e.getMessage());
            logger.error(e.getMessage(), e);
        }
        String json = JSON.toJSONString(result);
        response.getWriter().println(json);
    }
}
