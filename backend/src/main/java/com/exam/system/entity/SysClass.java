package com.exam.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("sys_class")
public class SysClass {
    @TableId(type = IdType.AUTO)
    private Integer classId;
    
    private String className;
    private String major;
    private String department;
    private Date createTime;
}
