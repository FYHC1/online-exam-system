package com.exam.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("student_answer_detail")
public class StudentAnswerDetail {
    @TableId(type = IdType.AUTO)
    private Integer detailId;
    
    private Integer recordId;
    private Integer questionId;
    private String answerContent;
    private Integer isCorrect; // 1 correct, 0 incorrect, null for subjective waiting grading
    private Integer score;
}
