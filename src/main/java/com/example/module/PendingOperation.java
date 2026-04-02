package com.example.module;

import lombok.Data;

/**
 * 确认操作实体
 */
@Data
public class PendingOperation {

    /**
     * 操作描述，说明要做什么
     */
    private String operation;

    /**
     * 操作的详细信息
     */
    private String details;

    /**
     * 操作的风险等级：low/medium/high
     */
    private String riskLevel;

    /**
     * 状态
     */
    private String status;

}
