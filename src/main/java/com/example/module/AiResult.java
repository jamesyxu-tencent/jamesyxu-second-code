package com.example.module;

import lombok.Data;

/**
 * 人员信息实体类
 * 用于从文本中提取个人基本信息
 */
@Data
public class AiResult {

    /**
     * AI回复
     */
    private String answer;

    /**
     * 消耗token数
     */
    private Integer tokens;

}