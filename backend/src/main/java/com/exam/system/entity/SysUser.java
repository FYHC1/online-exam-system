package com.exam.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.util.Date;
import java.util.List;

@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Integer userId;
    
    private String username;
    private String password;
    private String realName;
    private String role; // admin, teacher, student
    private String phone;
    private Integer classId;
    private Integer status; // 1 normal, 0 disabled
    private Date createTime;
    @TableField(exist = false)
    private String grade;
    @TableField(exist = false)
    private String department;
    @TableField(exist = false)
    private String major;
    @TableField(exist = false)
    private String className;
    @TableField(exist = false)
    private List<Integer> managedClassIds;
    @TableField(exist = false)
    private List<String> managedSubjects;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<Integer> getManagedClassIds() {
        return managedClassIds;
    }

    public void setManagedClassIds(List<Integer> managedClassIds) {
        this.managedClassIds = managedClassIds;
    }

    public List<String> getManagedSubjects() {
        return managedSubjects;
    }

    public void setManagedSubjects(List<String> managedSubjects) {
        this.managedSubjects = managedSubjects;
    }
}
