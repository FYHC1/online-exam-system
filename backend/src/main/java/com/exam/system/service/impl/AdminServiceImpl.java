package com.exam.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.exam.system.entity.*;
import com.exam.system.mapper.*;
import com.exam.system.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysClassMapper classMapper;
    @Autowired
    private SysAnnouncementMapper announcementMapper;
    @Autowired
    private ExamArrangementMapper examMapper;
    @Autowired
    private QuestionBankMapper questionMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("totalExams", examMapper.selectCount(null));
        stats.put("totalQuestions", questionMapper.selectCount(null));
        return stats;
    }

    @Override
    public List<SysUser> getUsers(String role, String keyword) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        if (role != null && !role.isEmpty()) wrapper.eq("role", role);
        if (keyword != null && !keyword.isEmpty()) wrapper.like("real_name", keyword);
        return userMapper.selectList(wrapper);
    }

    @Override
    public void addUser(SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }

    @Override
    public void updateUser(SysUser user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // prevent overwriting with null
        }
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(Integer userId) {
        userMapper.deleteById(userId);
    }

    @Override
    public List<SysClass> getClasses(String department) {
        QueryWrapper<SysClass> wrapper = new QueryWrapper<>();
        if (department != null && !department.isEmpty()) wrapper.eq("department", department);
        return classMapper.selectList(wrapper);
    }

    @Override
    public void addClass(SysClass sysClass) {
        classMapper.insert(sysClass);
    }

    @Override
    public void updateClass(SysClass sysClass) {
        classMapper.updateById(sysClass);
    }

    @Override
    public void deleteClass(Integer classId) {
        classMapper.deleteById(classId);
    }

    @Override
    public List<SysAnnouncement> getAnnouncements() {
        QueryWrapper<SysAnnouncement> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("is_top", "create_time");
        return announcementMapper.selectList(wrapper);
    }

    @Override
    public void addAnnouncement(SysAnnouncement announcement) {
        announcementMapper.insert(announcement);
    }

    @Override
    public void updateAnnouncement(SysAnnouncement announcement) {
        announcementMapper.updateById(announcement);
    }

    @Override
    public void deleteAnnouncement(Integer id) {
        announcementMapper.deleteById(id);
    }
}
