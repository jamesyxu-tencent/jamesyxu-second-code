package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.PromptTemplate;
import com.example.service.PromptTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/prompt-templates")
public class PromptTemplateController {

    @Autowired
    private PromptTemplateService promptTemplateService;

    @GetMapping("/render")
    public String renderTemplate(@RequestParam String name,
                                 @RequestParam Map<String, String> placeholders) {
        return promptTemplateService.getContentWithPlaceholders(name, placeholders);
    }

    @PostMapping("/copy")
    public PromptTemplate copyTemplate(@RequestParam Long id, @RequestParam String newName) {
        return promptTemplateService.copyTemplate(id, newName);
    }

    @GetMapping("/page")
    public IPage<PromptTemplate> page(@RequestParam(defaultValue = "1") int current,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) String isActive) {
        Page<PromptTemplate> page = new Page<>(current, size);
        return promptTemplateService.queryPage(page, keyword, category, isActive);
    }
}