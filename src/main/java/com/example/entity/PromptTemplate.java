package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("prompt_templates")
public class PromptTemplate extends BaseEntity {

    private String name;           // 模板名称，如 "java-expert"

    private String content;        // 模板内容，支持占位符 {placeholder}

    private String description;     // 模板描述

    private String category;        // 分类：编程/写作/翻译等

    private String version;    // 版本号

    private String isActive;

}
