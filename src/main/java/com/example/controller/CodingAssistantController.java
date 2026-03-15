package com.example.controller;

import com.example.service.ICodingAssistantService;
import com.example.vo.base.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coding")
public class CodingAssistantController {

    @Autowired
    private ICodingAssistantService codingService;

    /**
     * 询问java专家
     */
    @PostMapping("/ask")
    public ApiResult<String> ask(@RequestParam String question) {
        return ApiResult.success(codingService.ask(question));
    }

    /**
     * 代码审查
     */
    @PostMapping("/review")
    public ApiResult<String> reviewCode(
            @RequestParam String language,
            @RequestParam String code) {
        return ApiResult.success(codingService.codeReview(code, language));
    }

    /**
     * 生成单元测试
     */
    @PostMapping("/test")
    public ApiResult<String> generateTest(
            @RequestParam String language,
            @RequestParam(defaultValue = "JUnit 5") String framework,
            @RequestParam String code) {
        return ApiResult.success(codingService.generateUnitTest(code, language, framework));
    }

    /**
     * 解释代码
     */
    @PostMapping("/explain")
    public ApiResult<String> explainCode(
            @RequestParam String language,
            @RequestParam(defaultValue = "beginner") String level,
            @RequestParam String code) {
        return ApiResult.success(codingService.explainCode(code, language, level));
    }

    /**
     * 优化代码
     */
    @PostMapping("/optimize")
    public ApiResult<String> optimizeCode(
            @RequestParam String language,
            @RequestParam(defaultValue = "performance") String target,
            @RequestParam String code) {
        return ApiResult.success(codingService.optimizeCode(code, language, target));
    }

    /**
     * 从注释生成代码
     */
    @PostMapping("/generate")
    public ApiResult<String> generateFromComments(
            @RequestParam String language,
            @RequestParam String comments) {
        return ApiResult.success(codingService.generateFromComments(comments, language));
    }
}