package com.exam.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("test_paper")
public class TestPaper {
    @TableId(type = IdType.AUTO)
    private Integer paperId;
    
    private String paperName;
    private String subject;
    private Integer totalScore;
    private Integer passScore;
    private Integer duration;
    private Integer createBy;
    private Date createTime;
}
