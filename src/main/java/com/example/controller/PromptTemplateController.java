package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.PromptTemplate;
import com.example.service.PromptTemplateService;
import com.example.vo.base.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/prompt-templates")
public class PromptTemplateController {

    @Autowired
    private PromptTemplateService promptTemplateService;

    @GetMapping("/render")
    public ApiResult<String> renderTemplate(@RequestParam String name,
                                           @RequestParam Map<String, String> placeholders) {
        return ApiResult.success(promptTemplateService.getContentWithPlaceholders(name, placeholders));
    }

    @PostMapping("/copy")
    public ApiResult<PromptTemplate> copyTemplate(@RequestParam Long id, @RequestParam String newName) {
        return ApiResult.success(promptTemplateService.copyTemplate(id, newName));
    }

    @GetMapping("/page")
    public ApiResult<IPage<PromptTemplate>> page(@RequestParam(defaultValue = "1") int current,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) String isActive) {
        Page<PromptTemplate> page = new Page<>(current, size);
        return ApiResult.success(promptTemplateService.queryPage(page, keyword, category, isActive));
    }

    @PostMapping("/initDefaultTemplates")
    public ApiResult initDefaultTemplates() {
        promptTemplateService.initDefaultTemplates();
        return ApiResult.success();
    }

}