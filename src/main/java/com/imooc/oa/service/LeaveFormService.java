package com.imooc.oa.service;

import com.imooc.oa.dao.EmployeeDao;
import com.imooc.oa.dao.LeaveFormDao;
import com.imooc.oa.dao.ProcessFlowDao;
import com.imooc.oa.entity.Employee;
import com.imooc.oa.entity.LeaveForm;
import com.imooc.oa.entity.ProcessFlow;
import com.imooc.oa.service.exception.BussinessException;
import com.imooc.oa.utils.MybatisUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LeaveFormService {
    /**
     * 创建请假单
     * @param form 前端输入的请假表单数据
     * @return 持久化后的请假表单对象
     */
    public LeaveForm createLeaveForm(LeaveForm form) {
        LeaveForm savedForm = (LeaveForm) MybatisUtils.executeUpdate(sqlSession -> {
            /*
             * 1. 持久化form表单数据，8级一下员工表单状态为processing，8级（总经理）状态为approved
             * 2. 增加第一条流程数据，说明表单已提交，状态为complete
             * 3. 分情况创建其余流程数据
             *    3.1. 7级以下员工，生成部门经理审批任务，请假时间大于MANAGER_AUDIT_HOURS小时还需生成总经理审批任务
             *    3.2. 7级员工，生成总经理审批任务
             *    3.3. 8级员工，生成总经理审批任务，系统自动通过
             * */
            EmployeeDao employeeDao = sqlSession.getMapper(EmployeeDao.class);
            Employee employee = employeeDao.selectById(form.getEmployeeId());
            if(employee.getLevel() == 8) {
                form.setState("approved");
            } else {
                form.setState("process");
            }
            LeaveFormDao leaveFormDao = sqlSession.getMapper(LeaveFormDao.class);
            leaveFormDao.insert(form);

            ProcessFlowDao processFlowDao = sqlSession.getMapper(ProcessFlowDao.class);
            ProcessFlow flow1 = new ProcessFlow();
            flow1.setFormId(form.getFormId());
            flow1.setOperatorId(employee.getEmployeeId());
            flow1.setCreateTime(new Date());
            flow1.setOrderNo(1);
            flow1.setAction("apply");
            flow1.setState("complete");
            flow1.setIsLast(0);
            processFlowDao.insert(flow1);

            if(employee.getLevel() < 7) {
                Employee dmanager = employeeDao.selectLeader(employee);
                ProcessFlow flow2 = new ProcessFlow();
                flow2.setFormId(form.getFormId());
                flow2.setOperatorId(dmanager.getEmployeeId());
                flow2.setAction("audit");
                flow2.setCreateTime(new Date());
                flow2.setOrderNo(2);
                flow2.setState("process");
                long diff = form.getEndTime().getTime() - form.getStartTime().getTime();
                float hours = diff/(1000 * 60 * 60 * 1f);
                if(hours >= BussinessConstants.MANAGER_AUDIT_HOURS) {
                    flow2.setIsLast(0);
                    processFlowDao.insert(flow2);
                    Employee manager = employeeDao.selectLeader(dmanager);
                    ProcessFlow flow3 = new ProcessFlow();
                    flow3.setFormId(form.getFormId());
                    flow3.setOperatorId(manager.getEmployeeId());
                    flow3.setAction("audit");
                    flow3.setCreateTime(new Date());
                    flow3.setState("ready");
                    flow3.setOrderNo(3);
                    flow3.setIsLast(1);
                    processFlowDao.insert(flow3);
                } else {
                    flow2.setIsLast(1);
                    processFlowDao.insert(flow2);
                }
            } else if(employee.getLevel() == 7) {
                Employee manager = employeeDao.selectLeader(employee);
                ProcessFlow flow = new ProcessFlow();
                flow.setFormId(form.getFormId());
                flow.setOperatorId(manager.getEmployeeId());
                flow.setAction("audit");
                flow.setCreateTime(new Date());
                flow.setState("process");
                flow.setOrderNo(2);
                flow.setIsLast(1);
                processFlowDao.insert(flow);
            } else if(employee.getLevel() == 8) {
                ProcessFlow flow = new ProcessFlow();
                flow.setFormId(form.getFormId());
                flow.setOperatorId(employee.getEmployeeId());
                flow.setAction("audit");
                flow.setResult("approved");
                flow.setReason("自动通过");
                flow.setCreateTime(new Date());
                flow.setAuditTime(new Date());
                flow.setState("complete");
                flow.setOrderNo(2);
                flow.setIsLast(1);
                processFlowDao.insert(flow);
            }

            return form;
        });

        return savedForm;
    }

    /**
     * 查找符合调价您的请假单
     * @param pfState 请假单状态
     * @param operatorId 请假单审批负责人Id
     * @return
     */
    public List<Map> getLeaveFormList(String pfState, Long operatorId) {
        return (List<Map>) MybatisUtils.executeQuery(sqlSession -> {
            LeaveFormDao dao = sqlSession.getMapper(LeaveFormDao.class);
            List<Map> formList = dao.selectByParams(pfState, operatorId);
            return formList;
        });
    }

    public void audit(Long formId, Long operatorId, String result, String reason) {
        MybatisUtils.executeUpdate(sqlSession -> {
            // 1、无论同意/驳回，当前任务状态变为complete
            ProcessFlowDao processFlowDao = sqlSession.getMapper(ProcessFlowDao.class);
            List<ProcessFlow> flowList = processFlowDao.selectByFormId(formId);
            if(flowList.size() == 0) {
                throw new BussinessException("PF001", "无效的审批处理");
            }
            // 获取当前任务ProcessFlow对象
            List<ProcessFlow> processList = flowList.stream().filter(p -> p.getOperatorId() == operatorId && p.getState().equals("process")).collect(Collectors.toList());
            ProcessFlow processFlow = null;
            if(processList.size() == 0) {
                throw new BussinessException("PF002", "未找到待处理任务");
            } else {
                processFlow = processList.get(0);
                processFlow.setState("complete");
                processFlow.setResult(result);
                processFlow.setReason(reason);
                processFlow.setAuditTime(new Date());
                processFlowDao.update(processFlow);
            }

            // 2、如果当前任务是最后一个节点，流程结束，更新请假单状态为对应的approved/refused
            LeaveFormDao leaveFormDao = sqlSession.getMapper(LeaveFormDao.class);
            LeaveForm form = leaveFormDao.selectById(formId);
            if(processFlow.getIsLast() == 1) {
                form.setState(result);
                leaveFormDao.update(form);
            } else {
                // readyList包含所有后续节点
                List<ProcessFlow> readyList = flowList.stream().filter(p -> p.getState().equals("ready")).collect(Collectors.toList());
                // 3、如果当前任务不是最后一个节点且审批通过，那么下一个节点状态从ready变为process
                if(result.equals("approved")) {
                    ProcessFlow readyProcess = readyList.get(0);
                    readyProcess.setState("process");
                    processFlowDao.update(readyProcess);
                } else if(result.equals("refused")) {
                    // 4、如果当前任务不是最后一个节点且审批驳回，那么后续所有状态变为cancel，请假单状态变为refused
                    for(ProcessFlow p : readyList) {
                        p.setState("cancel");
                        processFlowDao.update(p);
                    }
                    form.setState("refused");
                    leaveFormDao.update(form);
                }
            }
            return null;
        });
    }
}
