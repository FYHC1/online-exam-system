package com.exam.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("sys_announcement")
public class SysAnnouncement {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String title;
    private String content;
    private Integer isTop;
    private Integer createBy;
    private Date createTime;
}
