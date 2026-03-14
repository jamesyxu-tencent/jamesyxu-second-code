package org.example.dto;

import lombok.Data;

/**
 * prompt组成
 */
@Data
public class CrispeRequestDTO {

    /**
     * 问题
     */
    private String question;

    /**
     * 角色
     */
    private String role;

    /**
     * 背景
     */
    private String background;

    /**
     * 风格
     */
    private String style;

    /**
     * 实验
     */
    private String experiment;

}
