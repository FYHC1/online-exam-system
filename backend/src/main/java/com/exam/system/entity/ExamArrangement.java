package com.exam.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("exam_arrangement")
public class ExamArrangement {
    @TableId(type = IdType.AUTO)
    private Integer examId;
    
    private String title;
    private Integer paperId;
    private String targetClasses; // comma separated class IDs
    private Date startTime;
    private Date endTime;
    private String status; // pending/running/finished
    private Integer createBy;
    private Date createTime;
}
