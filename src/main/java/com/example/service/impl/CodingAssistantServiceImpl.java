package com.example.service.impl;

import com.example.service.ICodingAssistantService;
import com.example.service.PromptTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CodingAssistantServiceImpl implements ICodingAssistantService {

    @Autowired
    private PromptTemplateService templateService;

    @Override
    public String ask(String question) {
        Map<String, Object> params = new HashMap<>();
        params.put("question", question);

        return templateService.generateWithTemplate("java-expert", params);
    }

    /**
     * 代码审查
     */
    @Override
    public String codeReview(String code, String language) {
        Map<String, Object> params = new HashMap<>();
        params.put("language", language);
        params.put("code", code);

        return templateService.generateWithTemplate("code-reviewer", params);
    }

    /**
     * 生成单元测试
     */
    @Override
    public String generateUnitTest(String code, String language, String framework) {
        Map<String, Object> params = new HashMap<>();
        params.put("language", language);
        params.put("framework", framework);
        params.put("code", code);

        return templateService.generateWithTemplate("test-generator", params);
    }

    /**
     * 解释代码
     */
    @Override
    public String explainCode(String code, String language, String level) {
        Map<String, Object> params = new HashMap<>();
        params.put("language", language);
        params.put("level", level);
        params.put("code", code);

        return templateService.generateWithTemplate("code-explainer", params);
    }

    /**
     * 代码优化建议
     */
    @Override
    public String optimizeCode(String code, String language, String target) {
        Map<String, Object> params = new HashMap<>();
        params.put("language", language);
        params.put("target", target);
        params.put("code", code);

        return templateService.generateWithTemplate("code-optimizer", params);
    }

    /**
     * 从注释生成代码
     */
    @Override
    public String generateFromComments(String code, String language) {
        Map<String, Object> params = new HashMap<>();
        params.put("language", language);
        params.put("code", code);

        return templateService.generateWithTemplate("code-generator", params);
    }

}