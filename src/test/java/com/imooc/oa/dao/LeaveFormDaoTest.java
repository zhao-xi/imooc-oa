package com.imooc.oa.dao;

import com.imooc.oa.entity.LeaveForm;
import com.imooc.oa.utils.MybatisUtils;
import junit.framework.TestCase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LeaveFormDaoTest extends TestCase {
    public void testInsert() {
        MybatisUtils.executeUpdate(sqlSession -> {
            LeaveFormDao dao = sqlSession.getMapper(LeaveFormDao.class);
            LeaveForm leaveForm = new LeaveForm();
            leaveForm.setEmployeeId(4l);
            leaveForm.setFormType(1);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startTime = null;
            Date endTime = null;
            try {
                startTime = sdf.parse("2020-03-25 08:00:00");
                endTime = sdf.parse("2020-04-01 18:00:00");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            leaveForm.setStartTime(startTime);
            leaveForm.setEndTime(endTime);
            leaveForm.setReason("回家探亲");
            leaveForm.setCreateTime(new Date());
            leaveForm.setState("processing");
            dao.insert(leaveForm);
            return null;
        });
    }

    public void testSelectByParams() {
        MybatisUtils.executeQuery(sqlSession -> {
            LeaveFormDao leaveFormDao = sqlSession.getMapper(LeaveFormDao.class);
            List<Map> list = leaveFormDao.selectByParams("process", 2l);
            System.out.println(list);
            return list;
        });
    }
}