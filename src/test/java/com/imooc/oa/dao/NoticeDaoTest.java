package com.imooc.oa.dao;

import com.imooc.oa.entity.Notice;
import com.imooc.oa.utils.MybatisUtils;
import junit.framework.TestCase;

import java.util.Date;

public class NoticeDaoTest extends TestCase {
    public void testInsert() {
        MybatisUtils.executeUpdate(sqlSession -> {
            NoticeDao dao = sqlSession.getMapper(NoticeDao.class);
            Notice notice = new Notice();
            notice.setReceiverId(2l);
            notice.setContent("测试消息");
            notice.setCreateTime(new Date());
            dao.insert(notice);
            return null;
        });
    }
}