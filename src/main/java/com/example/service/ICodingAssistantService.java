package com.example.service;

public interface ICodingAssistantService {

    /**
     * 询问java专家
     */
    String ask(String question);

    /**
     * 代码审查
     */
    String codeReview(String code, String language);

    /**
     * 生成单元测试
     */
    String generateUnitTest(String code, String language, String framework);

    /**
     * 解释代码
     */
    String explainCode(String code, String language, String level);

    /**
     * 代码优化建议
     */
    String optimizeCode(String code, String language, String target);

    /**
     * 从注释生成代码
     */
    String generateFromComments(String comments, String language);
}
