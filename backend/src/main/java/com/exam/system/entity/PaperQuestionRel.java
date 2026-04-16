package com.exam.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("paper_question_rel")
public class PaperQuestionRel {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer paperId;
    private Integer questionId;
    private Integer score;
    private Integer sortOrder;
}
