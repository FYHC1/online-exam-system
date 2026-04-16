package com.exam.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("student_exam_record")
public class StudentExamRecord {
    @TableId(type = IdType.AUTO)
    private Integer recordId;
    
    private Integer examId;
    private Integer studentId;
    private Integer totalScore;
    private Integer objectiveScore;
    private Integer subjectiveScore;
    private String status; // pending/grading/finished
}
