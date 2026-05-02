package com.exam.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.exam.system.common.Result;
import com.exam.system.entity.SysAnnouncement;
import com.exam.system.mapper.SysAnnouncementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/announcements")
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
public class AnnouncementController {

    @Autowired
    private SysAnnouncementMapper announcementMapper;

    @GetMapping
    public Result<List<SysAnnouncement>> getAnnouncements() {
        QueryWrapper<SysAnnouncement> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("is_top", "create_time");
        return Result.success(announcementMapper.selectList(wrapper));
    }
}
