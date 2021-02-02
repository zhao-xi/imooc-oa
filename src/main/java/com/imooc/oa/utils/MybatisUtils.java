package com.imooc.oa.utils;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.Reader;
import java.util.function.Function;

public class MybatisUtils {
    private static SqlSessionFactory sqlSessionFactory = null;
    static {
        Reader reader = null;
        try{
            reader = Resources.getResourceAsReader("mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // 用于执行查询select语句
    public static Object executeQuery(Function<SqlSession, Object> func) {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            Object obj = func.apply(session);
            return obj;
        } finally {
            session.close();
        }
    }

    // 用于执行insert/update/delete写操作
    public static Object executeUpdate(Function<SqlSession, Object> func) {
        SqlSession session = sqlSessionFactory.openSession(false); // autoCommit = false
        try {
            Object obj = func.apply(session);
            session.commit();
            return obj;
        } catch (RuntimeException e) {
            session.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}
