package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.PromptTemplate;
import com.example.mapper.PromptTemplateMapper;
import com.example.service.IChatService;
import com.example.service.PromptTemplateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提示词模板 Service 实现类
 */
@Service
@Slf4j
public class PromptTemplateServiceImpl extends ServiceImpl<PromptTemplateMapper, PromptTemplate> implements PromptTemplateService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(.*?)\\}");

    @Resource
    private IChatService chatService;

    @Override
    public List<PromptTemplate> getActiveByCategory(String category) {
        return baseMapper.selectActiveByCategory(category);
    }

    @Override
    public PromptTemplate getByName(String name) {
        return baseMapper.selectByName(name);
    }

    @Override
    public String getContentWithPlaceholders(String name, Map<String, String> placeholders) {
        PromptTemplate template = getByName(name);
        if (template == null || !"Y".equals(template.getIsActive())) {
            throw new RuntimeException("模板不存在或已禁用: " + name);
        }

        String content = template.getContent();
        if (placeholders == null || placeholders.isEmpty()) {
            return content;
        }

        // 替换所有占位符 {key}
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = placeholders.getOrDefault(key, "{" + key + "}");
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableTemplate(Long id) {
        PromptTemplate template = new PromptTemplate();
        template.setId(id);
        template.setIsActive("Y");
        return updateById(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableTemplate(Long id) {
        PromptTemplate template = new PromptTemplate();
        template.setId(id);
        template.setIsActive("N");
        return updateById(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateStatus(List<Long> ids, String isActive) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return baseMapper.batchUpdateStatus(ids, isActive) > 0;
    }

    @Override
    public IPage<PromptTemplate> queryPage(IPage<PromptTemplate> page, String keyword, String category, String isActive) {
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();

        // 关键词模糊搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(PromptTemplate::getName, keyword)
                    .or()
                    .like(PromptTemplate::getDescription, keyword)
                    .or()
                    .like(PromptTemplate::getContent, keyword)
            );
        }

        // 分类筛选
        if (StringUtils.hasText(category)) {
            wrapper.eq(PromptTemplate::getCategory, category);
        }

        // 状态筛选
        if (StringUtils.hasText(isActive)) {
            wrapper.eq(PromptTemplate::getIsActive, isActive);
        }

        // 按更新时间倒序
        wrapper.orderByDesc(PromptTemplate::getUpdateTime);

        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromptTemplate copyTemplate(Long id, String newName) {
        // 1. 查询原模板
        PromptTemplate original = getById(id);
        if (original == null) {
            throw new RuntimeException("模板不存在，ID: " + id);
        }

        // 2. 检查新名称是否唯一
        if (!checkNameUnique(newName, null)) {
            throw new RuntimeException("模板名称已存在: " + newName);
        }

        // 3. 创建新模板
        PromptTemplate newTemplate = new PromptTemplate();
        newTemplate.setName(newName);
        newTemplate.setContent(original.getContent());
        newTemplate.setDescription(original.getDescription());
        newTemplate.setCategory(original.getCategory());
        newTemplate.setVersion(incrementVersion(original.getVersion()));
        newTemplate.setIsActive("N"); // 新模板默认禁用，需手动启用

        // 4. 保存
        save(newTemplate);
        return newTemplate;
    }

    @Override
    public boolean checkNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplate::getName, name);

        if (excludeId != null) {
            wrapper.ne(PromptTemplate::getId, excludeId);
        }

        return count(wrapper) == 0;
    }

    @Override
    public String generateWithTemplate(String templateName,
                                       Map<String, Object> params) {
        PromptTemplate template = this.getByName(templateName);

        if (ObjectUtils.isEmpty(template)) {
            return "模板不存在: " + templateName;
        }

        String systemPrompt = template.getContent();

        // 替换模板中的参数占位符
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            systemPrompt = systemPrompt.replace(
                    "{" + entry.getKey() + "}",
                    entry.getValue().toString()
            );
        }

        log.info("systemPrompt: {}", systemPrompt);

        // 使用AiService中的方法（需要稍后添加一个接受systemPrompt的方法）
        return chatService.chat(systemPrompt);
    }

    @Override
    public void initDefaultTemplates() {
        // Java专家模板
        PromptTemplate javaExpert = new PromptTemplate();
        javaExpert.setName("java-expert");
        javaExpert.setCategory("programming");
        javaExpert.setContent("""
                你是一位Java架构师。
                请你回答以下问题：{question}。
                
                回答要求：
                1. 提供代码示例时，确保符合Java的语法
                2. 解释要深入浅出，兼顾原理和实践
                3. 如果涉及性能，给出具体的benchmark数据
                4. 指出常见的坑和最佳实践
                """);
        javaExpert.setDescription("Java专家角色模板，可用于技术问答");
        javaExpert.setVersion("1");
        this.save(javaExpert);

        // 翻译助手模板
        PromptTemplate translator = new PromptTemplate();
        translator.setName("translator");
        translator.setCategory("translation");
        translator.setContent("""
                你是一位专业翻译，精通{sourceLang}和{targetLang}。
                请将以下{sourceLang}内容翻译成{targetLang}：
                
                翻译要求：
                1. 保持原文的专业术语准确性
                2. 如果有多义词，根据上下文选择最合适的翻译
                3. 保留原文的格式（如换行、列表等）
                """);
        translator.setDescription("专业翻译模板");
        translator.setVersion("1");
        this.save(translator);

        // 写作助手模板
        PromptTemplate writer = new PromptTemplate();
        writer.setName("writer");
        writer.setCategory("writing");
        writer.setContent("""
                你是一位{type}作家，擅长写作{genre}类型的文章。
                
                写作要求：
                1. 文章风格：{style}
                2. 目标读者：{audience}
                3. 文章长度：{length}字左右
                4. 需要包含以下关键词：{keywords}
                5. {special_requirements}
                
                请根据以上要求，{question}。
                """);
        writer.setDescription("写作助手模板");
        writer.setVersion("1");
        this.save(writer);

        // 代码审查模板
        PromptTemplate codeReviewer = new PromptTemplate();
        codeReviewer.setName("code-reviewer");
        codeReviewer.setCategory("coding");
        codeReviewer.setContent("""
                你是一位资深的{language}代码审查专家。
                
                请审查以下代码，并从以下几个方面给出反馈：
                1. 潜在bug和逻辑错误
                2. 性能问题
                3. 代码规范问题
                4. 可读性和维护性
                5. 安全漏洞
                
                对于每个问题，请：
                - 指出具体的代码行号
                - 解释为什么这是个问题
                - 给出修改建议和示例代码
                
                最后给出总体评价和改进优先级。
                
                代码如下：{code}
                """);
        codeReviewer.setDescription("代码审查助手");
        codeReviewer.setVersion("1");
        this.save(codeReviewer);

        // 测试生成模板
        PromptTemplate testGenerator = new PromptTemplate();
        testGenerator.setName("test-generator");
        testGenerator.setCategory("coding");
        testGenerator.setContent("""
                你是一位测试驱动开发专家，精通{language}和{framework}测试框架。
                
                请为以下代码生成完整的单元测试：
                
                测试要求：
                1. 覆盖所有主要路径和边界条件
                2. 包括正常场景和异常场景
                3. 使用{framework}的最佳实践
                4. 包含必要的mock和stub
                5. 测试方法命名清晰，体现测试意图
                
                请生成完整的测试代码，并解释每个测试用例的测试目的。
                
                代码如下：{code}
                """);
        testGenerator.setDescription("单元测试生成器");
        testGenerator.setVersion("1");
        this.save(testGenerator);

        // 代码解释器模板
        PromptTemplate codeExplainer = new PromptTemplate();
        codeExplainer.setName("code-explainer");
        codeExplainer.setCategory("coding");
        codeExplainer.setContent("""
                你是一位编程导师，擅长用通俗易懂的方式解释技术概念。
                
                请解释以下代码，目标受众是{level}水平的开发者。
                
                解释要求：
                1. 先概述这段代码的整体功能
                2. 逐行/逐段解释关键逻辑
                3. 解释用到的关键API/库的作用
                4. 指出代码中的设计模式或重要概念
                5. 如果适用，画出代码执行流程图（用文字描述）
                
                让初学者也能理解这些代码！
                
                代码如下：{code}
                """);
        codeExplainer.setDescription("代码解释器");
        codeExplainer.setVersion("1");
        this.save(codeExplainer);

        // 代码优化器模板
        PromptTemplate codeOptimizer = new PromptTemplate();
        codeOptimizer.setName("code-optimizer");
        codeOptimizer.setCategory("coding");
        codeOptimizer.setContent("""
                你是一位性能优化专家，精通{language}。
                
                请优化以下代码，重点关注{target}。
                
                优化要求：
                1. 提供优化前后的代码对比
                2. 解释为什么优化后的代码更好
                3. 如果有性能数据，提供预估的提升幅度
                4. 考虑可读性和维护性的平衡
                5. 指出优化可能带来的副作用
                
                优化目标可以是：执行速度、内存使用、代码简洁性、可维护性等。
                
                代码如下：{code}
                """);
        codeOptimizer.setDescription("代码优化器");
        codeOptimizer.setVersion("1");
        this.save(codeOptimizer);

        // 代码生成器模板
        PromptTemplate codeGenerator = new PromptTemplate();
        codeGenerator.setName("code-generator");
        codeGenerator.setCategory("coding");
        codeGenerator.setContent("""
                你是一位{language}开发专家，擅长根据需求生成高质量代码。
                
                请根据以下需求生成{language}代码：
                
                代码要求：
                1. 遵循{language}的最佳实践和编码规范
                2. 包含必要的注释
                3. 考虑异常处理和边界条件
                4. 代码结构清晰，易于维护
                5. 如果适用，考虑性能优化
                
                除了代码，请简要解释你的设计思路。
                
                代码如下：{code}
                """);
        codeGenerator.setDescription("代码生成器");
        codeGenerator.setVersion("1");
        this.save(codeGenerator);
    }

    /**
     * 版本号递增
     *
     * @param version 原版本号
     * @return 新版本号
     */
    private String incrementVersion(String version) {
        if (version == null || version.isEmpty()) {
            return "1.0.0";
        }

        try {
            String[] parts = version.split("\\.");
            if (parts.length >= 3) {
                int minor = Integer.parseInt(parts[2]) + 1;
                return parts[0] + "." + parts[1] + "." + minor;
            }
        } catch (NumberFormatException e) {
            // 忽略解析错误
        }

        return version + ".1";
    }
}